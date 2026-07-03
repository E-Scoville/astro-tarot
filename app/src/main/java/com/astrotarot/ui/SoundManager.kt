package com.astrotarot.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

val LocalSoundManager = compositionLocalOf<SoundManager?> { null }

class SoundManager(context: Context) {

    private val appContext = context.applicationContext
    private val scope      = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── SoundPool for short SFX ───────────────────────────────────────────────

    private val pool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var flipId   = 0
    private var revealId = 0
    private val ready    = mutableSetOf<Int>()

    // ── MediaPlayer for ambient drone ─────────────────────────────────────────

    private var dronePlayer: MediaPlayer? = null

    var muted = false
        set(value) {
            field = value
            if (value) dronePlayer?.pause() else dronePlayer?.start()
        }

    companion object {
        private const val DRONE_VOL = 0.65f
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    init {
        pool.setOnLoadCompleteListener { _, id, status ->
            if (status == 0) ready.add(id)
        }
        scope.launch {
            val cacheDir   = appContext.cacheDir
            val flipFile   = File(cacheDir, "snd_flip.wav")
            val revealFile = File(cacheDir, "snd_reveal.wav")
            val droneFile  = File(cacheDir, "snd_drone.wav")

            writeWav(flipFile,   generateFlipPcm())
            writeWav(revealFile, generateRevealPcm())

            flipId   = pool.load(flipFile.absolutePath,   1)
            revealId = pool.load(revealFile.absolutePath, 1)

            // Drone disabled — pending redesign
            // withContext(Dispatchers.Main) { startDronePlayer(droneFile) }
        }
    }

    // ── Public playback API ───────────────────────────────────────────────────

    fun playFlip()   { if (!muted && flipId   in ready) pool.play(flipId,   0.60f, 0.60f, 1, 0, 1.0f) }
    fun playReveal() { if (!muted && revealId in ready) pool.play(revealId, 0.75f, 0.75f, 1, 0, 1.0f) }

    fun pauseDrone()  { dronePlayer?.pause() }
    fun resumeDrone() { if (!muted) dronePlayer?.start() }

    fun release() {
        scope.cancel()
        pool.release()
        dronePlayer?.apply { stop(); release() }
        dronePlayer = null
    }

    // ── MediaPlayer setup ─────────────────────────────────────────────────────

    private fun startDronePlayer(file: File) {
        dronePlayer = MediaPlayer().apply {
            java.io.FileInputStream(file).use { fis -> setDataSource(fis.fd) }
            isLooping = true
            setVolume(DRONE_VOL, DRONE_VOL)
            setOnPreparedListener { it.start() }
            setOnErrorListener { _, what, extra ->
                android.util.Log.e("SoundManager", "drone MediaPlayer error what=$what extra=$extra")
                false
            }
            prepareAsync()
        }
    }

    // ── Soundbath drone — crystal singing bowl simulation, seamless 16 s loop ──
    //
    // Crystal bowls live in the 256–768 Hz range (not bass). Their characteristic
    // "singing" quality comes from FM synthesis: a slow frequency wobble at ~3.5 Hz
    // (like a mallet rubbing the rim) modulates the carrier slightly.
    //
    // All carrier frequencies × 16 = integer  →  zero phase at loop boundary  →  seamless.
    // FM modulator 3.5 Hz × 16 = 56 cycles    →  also zero at loop boundary  ✓
    // All envelope LFOs are multiples of 1/16 Hz → also seamless ✓
    //
    // Bowls (Just Intonation based on 256 Hz):
    //   256 Hz  — C4 "middle" bowl (primary)
    //   384 Hz  — G4 perfect fifth
    //   512 Hz  — C5 octave
    //   528 Hz  — "miracle" Solfeggio tone (near C5, gentle beating with 512)
    //   768 Hz  — G5 bright shimmer
    // Each has a slightly detuned copy (± 1/16 Hz) for chorus shimmer.

    private fun generateDronePcm(sampleRate: Int = 44100): ShortArray {
        val loopSec = 16.0
        val n       = (sampleRate * loopSec).toInt()
        val out     = ShortArray(n)

        // (carrier Hz, amplitude, envelope Hz, envelope phase offset)
        // Envelope: 0.25 + 0.75*(0.5 + 0.5*sin(2π·envHz·t + phase))  →  range [0.25, 1.0]
        data class Bowl(val freq: Double, val amp: Double, val envHz: Double, val envPhase: Double)

        val bowls = listOf(
            Bowl(256.0000, 0.34, 0.0625, 0.0),          // C4 primary
            Bowl(256.0625, 0.17, 0.1250, PI / 3.0),     // C4 shimmer
            Bowl(384.0000, 0.27, 0.1250, PI / 2.0),     // G4 fifth
            Bowl(384.0625, 0.13, 0.0625, PI),            // G4 shimmer
            Bowl(512.0000, 0.19, 0.1875, PI * 1.5),     // C5 octave
            Bowl(512.0625, 0.09, 0.1250, PI * 0.75),    // C5 shimmer
            Bowl(528.0000, 0.14, 0.0625, PI * 1.25),    // 528 Hz miracle tone
            Bowl(768.0000, 0.09, 0.1875, PI * 0.33),    // G5 high sparkle
            Bowl(768.0625, 0.04, 0.2500, PI * 1.66),    // G5 shimmer
        )
        val totalAmp = bowls.sumOf { it.amp }

        // FM: 3.5 Hz "mallet on rim" wobble — gives organic singing-bowl quality
        val fmHz    = 3.5
        val fmDepth = 0.10  // radians (subtle)

        for (i in 0 until n) {
            val t   = i.toDouble() / sampleRate
            val fm  = fmDepth * sin(2.0 * PI * fmHz * t)

            var s = 0.0
            for (bowl in bowls) {
                val env   = 0.25 + 0.75 * (0.5 + 0.5 * sin(2.0 * PI * bowl.envHz * t + bowl.envPhase))
                val phase = 2.0 * PI * bowl.freq * t + fm * (bowl.freq / 256.0)
                s += bowl.amp * env * sin(phase)
            }
            out[i] = (s / totalAmp * Short.MAX_VALUE * 0.78)
                .toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // ── Flip — filtered noise sweep (~180 ms) ─────────────────────────────────

    private fun generateFlipPcm(sampleRate: Int = 44100): ShortArray {
        val n   = sampleRate * 180 / 1000
        val out = ShortArray(n)
        val rng = java.util.Random(7L)
        for (i in 0 until n) {
            val t    = i.toDouble() / n
            val tSec = i.toDouble() / sampleRate
            val env  = if (t < 0.04) t / 0.04 else exp(-6.0 * (t - 0.04))
            val freq   = 600.0 - 400.0 * t
            val sweep  = sin(2.0 * PI * freq * tSec)
            val noise  = rng.nextGaussian()
            val sample = (0.55 * noise + 0.45 * sweep) * env
            out[i] = (sample * Short.MAX_VALUE * 0.48)
                .toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // ── Reveal — 528 Hz bell chime with inharmonic partials (~1.8 s) ──────────

    private fun generateRevealPcm(sampleRate: Int = 44100): ShortArray {
        val n   = (sampleRate * 1.8).toInt()
        val out = ShortArray(n)
        val partials = listOf(
            Triple(1.000, 1.00, 2.2),
            Triple(2.756, 0.55, 3.8),
            Triple(5.404, 0.28, 6.0),
            Triple(8.933, 0.14, 9.0),
        )
        val fundamental = 528.0
        val totalAmp    = partials.sumOf { it.second }
        for (i in 0 until n) {
            val tSec   = i.toDouble() / sampleRate
            val attack = min(1.0, tSec / 0.004)
            var s      = 0.0
            for ((ratio, amp, decay) in partials)
                s += amp * exp(-decay * tSec) * sin(2.0 * PI * fundamental * ratio * tSec)
            out[i] = (s / totalAmp * attack * Short.MAX_VALUE * 0.72)
                .toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
        return out
    }

    // ── WAV writer (16-bit mono PCM) ──────────────────────────────────────────

    private fun writeWav(file: File, pcm: ShortArray, sampleRate: Int = 44100) {
        val dataBytes = pcm.size * 2
        file.outputStream().buffered().use { out ->
            fun le32(v: Int) = out.write(byteArrayOf(
                (v and 0xFF).toByte(), (v shr 8 and 0xFF).toByte(),
                (v shr 16 and 0xFF).toByte(), (v shr 24 and 0xFF).toByte()))
            fun le16(v: Int) = out.write(byteArrayOf(
                (v and 0xFF).toByte(), (v shr 8 and 0xFF).toByte()))
            out.write("RIFF".toByteArray());  le32(36 + dataBytes)
            out.write("WAVEfmt ".toByteArray()); le32(16)
            le16(1); le16(1); le32(sampleRate); le32(sampleRate * 2); le16(2); le16(16)
            out.write("data".toByteArray());  le32(dataBytes)
            val bytes = ByteArray(dataBytes)
            for (i in pcm.indices) {
                bytes[i * 2]     = (pcm[i].toInt() and 0xFF).toByte()
                bytes[i * 2 + 1] = (pcm[i].toInt() shr 8 and 0xFF).toByte()
            }
            out.write(bytes)
        }
    }
}
