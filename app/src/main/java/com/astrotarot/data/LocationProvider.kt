package com.astrotarot.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Abstracts device location so the ViewModel can be unit-tested without Play Services. */
interface LocationProvider {
    /** Returns (latitude, longitude) or throws if no fix could be obtained. */
    suspend fun currentCoordinates(): Pair<Double, Double>
}

class FusedLocationProvider(context: Context) : LocationProvider {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun currentCoordinates(): Pair<Double, Double> {
        // Try last known location first — this responds instantly to emulator
        // mock locations (adb emu geo fix) and is cheaper than a fresh fix.
        val last = suspendCancellableCoroutine<android.location.Location?> { cont ->
            fusedClient.lastLocation
                .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                .addOnFailureListener { if (cont.isActive) cont.resume(null) }
        }
        if (last != null) return last.latitude to last.longitude

        // Fall back to a fresh location fix (real device, cold GPS).
        val fresh = suspendCancellableCoroutine<android.location.Location?> { cont ->
            val cts = CancellationTokenSource()
            cont.invokeOnCancellation { cts.cancel() }
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(15_000L)
                .build()
            fusedClient.getCurrentLocation(request, cts.token)
                .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
        }
        return fresh?.latitude?.let { it to fresh.longitude }
            ?: throw Exception("Could not determine location.\nTry entering coordinates manually.")
    }
}
