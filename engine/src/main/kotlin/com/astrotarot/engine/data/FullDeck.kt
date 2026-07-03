package com.astrotarot.engine.data

import com.astrotarot.engine.domain.model.*

val FULL_DECK: List<TarotCard> = buildList {

    // ==================== MAJOR ARCANA (22) ====================

    add(TarotCard("The Fool", ArcanaType.MAJOR,
        associatedBody = CelestialBody.URANUS,
        baseDescription = "New beginnings, spontaneity, a leap of faith.",
        reversedDescription = "Recklessness, naivety, poor judgment."))

    add(TarotCard("The Magician", ArcanaType.MAJOR,
        associatedBody = CelestialBody.MERCURY,
        baseDescription = "Willpower, skill, resourcefulness, manifestation.",
        reversedDescription = "Manipulation, trickery, wasted talent."))

    add(TarotCard("The High Priestess", ArcanaType.MAJOR,
        associatedBody = CelestialBody.MOON,
        baseDescription = "Intuition, sacred knowledge, the subconscious.",
        reversedDescription = "Repressed intuition, hidden agendas, disconnection from self."))

    add(TarotCard("The Empress", ArcanaType.MAJOR,
        associatedBody = CelestialBody.VENUS,
        baseDescription = "Fertility, abundance, nurturing, creative power.",
        reversedDescription = "Dependence, creative block, smothering."))

    add(TarotCard("The Emperor", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.ARIES,
        baseDescription = "Authority, structure, stability, leadership.",
        reversedDescription = "Domination, rigidity, excessive control, tyranny."))

    add(TarotCard("The Hierophant", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.TAURUS,
        baseDescription = "Tradition, spiritual wisdom, institutions, mentorship.",
        reversedDescription = "Rebellion, unorthodoxy, subversive beliefs."))

    add(TarotCard("The Lovers", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.GEMINI,
        baseDescription = "Love, harmony, alignment of values, important choices.",
        reversedDescription = "Imbalance, misalignment, poor choices, inner conflict."))

    add(TarotCard("The Chariot", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.CANCER,
        baseDescription = "Control, willpower, victory, determination.",
        reversedDescription = "Aggression, lack of direction, defeat, loss of control."))

    add(TarotCard("Strength", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.LEO,
        baseDescription = "Courage, patience, inner strength, compassion.",
        reversedDescription = "Self-doubt, weakness, raw emotion, insecurity."))

    add(TarotCard("The Hermit", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.VIRGO,
        baseDescription = "Solitude, soul-searching, inner guidance, contemplation.",
        reversedDescription = "Isolation, loneliness, withdrawal, avoidance."))

    add(TarotCard("Wheel of Fortune", ArcanaType.MAJOR,
        associatedBody = CelestialBody.JUPITER,
        baseDescription = "Good luck, karma, life cycles, a turning point.",
        reversedDescription = "Bad luck, resistance to change, breaking cycles."))

    add(TarotCard("Justice", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.LIBRA,
        baseDescription = "Fairness, truth, cause and effect, the law.",
        reversedDescription = "Unfairness, dishonesty, avoidance of accountability."))

    add(TarotCard("The Hanged Man", ArcanaType.MAJOR,
        associatedBody = CelestialBody.NEPTUNE,
        baseDescription = "Surrender, new perspectives, suspension, waiting.",
        reversedDescription = "Delays, resistance, indecision, stagnation."))

    add(TarotCard("Death", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.SCORPIO,
        baseDescription = "Endings, transformation, transition, letting go.",
        reversedDescription = "Resistance to change, inability to move on, stagnation."))

    add(TarotCard("Temperance", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.SAGITTARIUS,
        baseDescription = "Balance, moderation, patience, divine timing.",
        reversedDescription = "Imbalance, excess, self-healing needed, realignment."))

    add(TarotCard("The Devil", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.CAPRICORN,
        baseDescription = "Shadow self, attachment, addiction, restriction.",
        reversedDescription = "Releasing limiting beliefs, detachment, reclaiming power."))

    add(TarotCard("The Tower", ArcanaType.MAJOR,
        associatedBody = CelestialBody.MARS,
        baseDescription = "Sudden upheaval, chaos, revelation, dismantling illusions.",
        reversedDescription = "Averting disaster, fear of change, delayed upheaval."))

    add(TarotCard("The Star", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.AQUARIUS,
        baseDescription = "Hope, faith, renewal, serenity, inspiration.",
        reversedDescription = "Despair, lack of faith, disconnection, insecurity."))

    add(TarotCard("The Moon", ArcanaType.MAJOR,
        associatedSign = ZodiacSign.PISCES,
        baseDescription = "Illusion, fear, the unconscious, confusion.",
        reversedDescription = "Release of fear, clarity emerging, repressed emotions surfacing."))

    add(TarotCard("The Sun", ArcanaType.MAJOR,
        associatedBody = CelestialBody.SUN,
        baseDescription = "Joy, success, vitality, positivity, abundance.",
        reversedDescription = "Inner child suppressed, pessimism, temporary setbacks."))

    add(TarotCard("Judgement", ArcanaType.MAJOR,
        associatedBody = CelestialBody.PLUTO,
        baseDescription = "Reflection, reckoning, awakening, absolution, rebirth.",
        reversedDescription = "Self-doubt, refusal of self-examination, false accusations."))

    add(TarotCard("The World", ArcanaType.MAJOR,
        associatedBody = CelestialBody.SATURN,
        baseDescription = "Completion, integration, accomplishment, wholeness.",
        reversedDescription = "Seeking closure, delays, carrying incomplete burdens."))

    // ==================== ACES (4) ====================

    add(TarotCard("Ace of Wands", ArcanaType.MINOR_ACE, suit = Suit.WANDS,
        baseDescription = "New inspiration, creative spark, potential, passion.",
        reversedDescription = "Delays in new ventures, creative blocks, lack of direction."))

    add(TarotCard("Ace of Cups", ArcanaType.MINOR_ACE, suit = Suit.CUPS,
        baseDescription = "New emotional beginnings, love, intuition, spirituality.",
        reversedDescription = "Emotional loss, blocked intuition, emptiness."))

    add(TarotCard("Ace of Swords", ArcanaType.MINOR_ACE, suit = Suit.SWORDS,
        baseDescription = "Mental clarity, breakthrough, truth, new ideas.",
        reversedDescription = "Confusion, chaos, clouded judgment, false starts."))

    add(TarotCard("Ace of Pentacles", ArcanaType.MINOR_ACE, suit = Suit.PENTACLES,
        baseDescription = "New financial opportunity, abundance, manifestation.",
        reversedDescription = "Lost opportunity, lack of planning, poor investments."))

    // ==================== WANDS 2–10 ====================

    add(TarotCard("Two of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.MARS, associatedSign = ZodiacSign.ARIES,
        longitudeRangeStart = 0.0, longitudeRangeEnd = 10.0,
        baseDescription = "Planning, future vision, discovery, bold expansion.",
        reversedDescription = "Lack of planning, fear of the unknown, poor decisions."))

    add(TarotCard("Three of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.SUN, associatedSign = ZodiacSign.ARIES,
        longitudeRangeStart = 10.0, longitudeRangeEnd = 20.0,
        baseDescription = "Progress, expansion, foresight, long-term plans underway.",
        reversedDescription = "Obstacles, delays, playing it too safe."))

    add(TarotCard("Four of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.VENUS, associatedSign = ZodiacSign.ARIES,
        longitudeRangeStart = 20.0, longitudeRangeEnd = 30.0,
        baseDescription = "Celebration, harmony, homecoming, community.",
        reversedDescription = "Lack of support, instability, discord at home."))

    add(TarotCard("Five of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.SATURN, associatedSign = ZodiacSign.LEO,
        longitudeRangeStart = 120.0, longitudeRangeEnd = 130.0,
        baseDescription = "Conflict, competition, disagreement, creative tension.",
        reversedDescription = "Avoiding conflict, inner turmoil, suppressed aggression."))

    add(TarotCard("Six of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.JUPITER, associatedSign = ZodiacSign.LEO,
        longitudeRangeStart = 130.0, longitudeRangeEnd = 140.0,
        baseDescription = "Victory, public recognition, success, self-confidence.",
        reversedDescription = "Private achievement, egotism, fall from grace."))

    add(TarotCard("Seven of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.MARS, associatedSign = ZodiacSign.LEO,
        longitudeRangeStart = 140.0, longitudeRangeEnd = 150.0,
        baseDescription = "Perseverance, defensiveness, maintaining position.",
        reversedDescription = "Exhaustion, giving up, overwhelmed."))

    add(TarotCard("Eight of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.MERCURY, associatedSign = ZodiacSign.SAGITTARIUS,
        longitudeRangeStart = 240.0, longitudeRangeEnd = 250.0,
        baseDescription = "Swift action, rapid progress, movement, communication.",
        reversedDescription = "Delays, frustration, miscommunication, lost momentum."))

    add(TarotCard("Nine of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.MOON, associatedSign = ZodiacSign.SAGITTARIUS,
        longitudeRangeStart = 250.0, longitudeRangeEnd = 260.0,
        baseDescription = "Resilience, persistence, last stand, defensiveness.",
        reversedDescription = "Stubbornness, rigid thinking, refusing to cooperate."))

    add(TarotCard("Ten of Wands", ArcanaType.MINOR_NUMBERED, suit = Suit.WANDS,
        associatedBody = CelestialBody.SATURN, associatedSign = ZodiacSign.SAGITTARIUS,
        longitudeRangeStart = 260.0, longitudeRangeEnd = 270.0,
        baseDescription = "Burden, extra responsibility, hard work, completion.",
        reversedDescription = "Overload, inability to delegate, burned out."))

    // ==================== CUPS 2–10 ====================

    add(TarotCard("Two of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.VENUS, associatedSign = ZodiacSign.CANCER,
        longitudeRangeStart = 90.0, longitudeRangeEnd = 100.0,
        baseDescription = "Mutual attraction, partnership, unity, connection.",
        reversedDescription = "Imbalance in relationship, broken agreements, disharmony."))

    add(TarotCard("Three of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.MERCURY, associatedSign = ZodiacSign.CANCER,
        longitudeRangeStart = 100.0, longitudeRangeEnd = 110.0,
        baseDescription = "Celebration, friendship, community, collaboration.",
        reversedDescription = "Overindulgence, gossip, isolation, cancelled plans."))

    add(TarotCard("Four of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.MOON, associatedSign = ZodiacSign.CANCER,
        longitudeRangeStart = 110.0, longitudeRangeEnd = 120.0,
        baseDescription = "Meditation, apathy, reevaluation, missed opportunities.",
        reversedDescription = "Returning clarity, new motivation, seizing opportunities."))

    add(TarotCard("Five of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.MARS, associatedSign = ZodiacSign.SCORPIO,
        longitudeRangeStart = 210.0, longitudeRangeEnd = 220.0,
        baseDescription = "Regret, loss, disappointment, grief, mourning.",
        reversedDescription = "Moving on, forgiveness, acceptance, recovery."))

    add(TarotCard("Six of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.SUN, associatedSign = ZodiacSign.SCORPIO,
        longitudeRangeStart = 220.0, longitudeRangeEnd = 230.0,
        baseDescription = "Nostalgia, childhood memories, innocence, reunion.",
        reversedDescription = "Living in the past, naivety, leaving home."))

    add(TarotCard("Seven of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.VENUS, associatedSign = ZodiacSign.SCORPIO,
        longitudeRangeStart = 230.0, longitudeRangeEnd = 240.0,
        baseDescription = "Choices, fantasy, illusion, wishful thinking.",
        reversedDescription = "Alignment, clarity, making decisions, grounded thinking."))

    add(TarotCard("Eight of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.SATURN, associatedSign = ZodiacSign.PISCES,
        longitudeRangeStart = 330.0, longitudeRangeEnd = 340.0,
        baseDescription = "Walking away, disillusionment, abandonment, seeking truth.",
        reversedDescription = "Aimless drifting, fear of commitment, staying in a bad situation."))

    add(TarotCard("Nine of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.JUPITER, associatedSign = ZodiacSign.PISCES,
        longitudeRangeStart = 340.0, longitudeRangeEnd = 350.0,
        baseDescription = "Contentment, satisfaction, wish fulfillment, emotional stability.",
        reversedDescription = "Greed, overindulgence, unhappiness beneath the surface."))

    add(TarotCard("Ten of Cups", ArcanaType.MINOR_NUMBERED, suit = Suit.CUPS,
        associatedBody = CelestialBody.MARS, associatedSign = ZodiacSign.PISCES,
        longitudeRangeStart = 350.0, longitudeRangeEnd = 360.0,
        baseDescription = "Emotional fulfillment, bliss, lasting happiness, family.",
        reversedDescription = "Family conflict, misaligned values, broken home."))

    // ==================== SWORDS 2–10 ====================

    add(TarotCard("Two of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.MOON, associatedSign = ZodiacSign.LIBRA,
        longitudeRangeStart = 180.0, longitudeRangeEnd = 190.0,
        baseDescription = "Difficult choices, stalemate, blocked emotions, indecision.",
        reversedDescription = "Confusion, information overload, decision finally made."))

    add(TarotCard("Three of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.SATURN, associatedSign = ZodiacSign.LIBRA,
        longitudeRangeStart = 190.0, longitudeRangeEnd = 200.0,
        baseDescription = "Heartbreak, grief, sorrow, emotional pain.",
        reversedDescription = "Recovery, moving on, forgiveness, optimism returning."))

    add(TarotCard("Four of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.JUPITER, associatedSign = ZodiacSign.LIBRA,
        longitudeRangeStart = 200.0, longitudeRangeEnd = 210.0,
        baseDescription = "Rest, recuperation, passive contemplation, sanctuary.",
        reversedDescription = "Burnout, restlessness, forced into action too soon."))

    add(TarotCard("Five of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.VENUS, associatedSign = ZodiacSign.AQUARIUS,
        longitudeRangeStart = 300.0, longitudeRangeEnd = 310.0,
        baseDescription = "Conflict, defeat, win at all costs, betrayal.",
        reversedDescription = "Reconciliation, past conflict returning, desire to move on."))

    add(TarotCard("Six of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.MERCURY, associatedSign = ZodiacSign.AQUARIUS,
        longitudeRangeStart = 310.0, longitudeRangeEnd = 320.0,
        baseDescription = "Transition, change, rite of passage, moving away from turbulence.",
        reversedDescription = "Unable to move on, emotional baggage, resistance to transition."))

    add(TarotCard("Seven of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.MOON, associatedSign = ZodiacSign.AQUARIUS,
        longitudeRangeStart = 320.0, longitudeRangeEnd = 330.0,
        baseDescription = "Deception, trickery, cunning, getting away with something.",
        reversedDescription = "Confession, coming clean, caught in a lie."))

    add(TarotCard("Eight of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.JUPITER, associatedSign = ZodiacSign.GEMINI,
        longitudeRangeStart = 60.0, longitudeRangeEnd = 70.0,
        baseDescription = "Restriction, imprisonment, victim mentality, self-imposed limitation.",
        reversedDescription = "Self-acceptance, new perspective, freedom, releasing limitations."))

    add(TarotCard("Nine of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.MARS, associatedSign = ZodiacSign.GEMINI,
        longitudeRangeStart = 70.0, longitudeRangeEnd = 80.0,
        baseDescription = "Anxiety, worry, fear, nightmares, mental anguish.",
        reversedDescription = "Finding hope, releasing anxiety, despairing less."))

    add(TarotCard("Ten of Swords", ArcanaType.MINOR_NUMBERED, suit = Suit.SWORDS,
        associatedBody = CelestialBody.SUN, associatedSign = ZodiacSign.GEMINI,
        longitudeRangeStart = 80.0, longitudeRangeEnd = 90.0,
        baseDescription = "Painful endings, deep wounds, betrayal, loss, crisis.",
        reversedDescription = "Recovery, regeneration, resisting the inevitable."))

    // ==================== PENTACLES 2–10 ====================

    add(TarotCard("Two of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.JUPITER, associatedSign = ZodiacSign.CAPRICORN,
        longitudeRangeStart = 270.0, longitudeRangeEnd = 280.0,
        baseDescription = "Balancing resources, adaptability, time management.",
        reversedDescription = "Overwhelmed, disorganized, poor financial management."))

    add(TarotCard("Three of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.MARS, associatedSign = ZodiacSign.CAPRICORN,
        longitudeRangeStart = 280.0, longitudeRangeEnd = 290.0,
        baseDescription = "Teamwork, collaboration, building, learning, skill.",
        reversedDescription = "Lack of teamwork, disorganization, group conflict."))

    add(TarotCard("Four of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.SUN, associatedSign = ZodiacSign.CAPRICORN,
        longitudeRangeStart = 290.0, longitudeRangeEnd = 300.0,
        baseDescription = "Control over finances, security, conservatism, possessiveness.",
        reversedDescription = "Greed, materialism, financial insecurity, overspending."))

    add(TarotCard("Five of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.MERCURY, associatedSign = ZodiacSign.TAURUS,
        longitudeRangeStart = 30.0, longitudeRangeEnd = 40.0,
        baseDescription = "Financial loss, poverty, isolation, worry, hardship.",
        reversedDescription = "Recovery, improvement in finances, spiritual poverty."))

    add(TarotCard("Six of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.MOON, associatedSign = ZodiacSign.TAURUS,
        longitudeRangeStart = 40.0, longitudeRangeEnd = 50.0,
        baseDescription = "Generosity, charity, giving and receiving, sharing wealth.",
        reversedDescription = "Strings-attached gifts, power dynamics, debt."))

    add(TarotCard("Seven of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.SATURN, associatedSign = ZodiacSign.TAURUS,
        longitudeRangeStart = 50.0, longitudeRangeEnd = 60.0,
        baseDescription = "Long-term vision, sustainable results, investment, patience.",
        reversedDescription = "Lack of long-term vision, limited success, impatience."))

    add(TarotCard("Eight of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.SUN, associatedSign = ZodiacSign.VIRGO,
        longitudeRangeStart = 150.0, longitudeRangeEnd = 160.0,
        baseDescription = "Apprenticeship, craftsmanship, skill development, diligence.",
        reversedDescription = "Perfectionism, lack of ambition, uninspired repetition."))

    add(TarotCard("Nine of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.VENUS, associatedSign = ZodiacSign.VIRGO,
        longitudeRangeStart = 160.0, longitudeRangeEnd = 170.0,
        baseDescription = "Abundance, luxury, self-sufficiency, achievement.",
        reversedDescription = "Self-worth issues, reckless spending, superficiality."))

    add(TarotCard("Ten of Pentacles", ArcanaType.MINOR_NUMBERED, suit = Suit.PENTACLES,
        associatedBody = CelestialBody.MERCURY, associatedSign = ZodiacSign.VIRGO,
        longitudeRangeStart = 170.0, longitudeRangeEnd = 180.0,
        baseDescription = "Wealth, legacy, inheritance, family, long-term success.",
        reversedDescription = "Financial failure, gambling, instability, family disputes."))

    // ==================== PAGES (4) ====================

    add(TarotCard("Page of Wands", ArcanaType.MINOR_COURT, suit = Suit.WANDS,
        baseDescription = "Exploration, excitement, freedom, fresh energy, news.",
        reversedDescription = "Immaturity, lack of direction, pessimism, creative blocks."))

    add(TarotCard("Page of Cups", ArcanaType.MINOR_COURT, suit = Suit.CUPS,
        baseDescription = "Creative opportunity, intuitive messages, curiosity, imagination.",
        reversedDescription = "Emotional immaturity, insecurity, repressed creativity."))

    add(TarotCard("Page of Swords", ArcanaType.MINOR_COURT, suit = Suit.SWORDS,
        baseDescription = "New ideas, curiosity, restlessness, sharp communication.",
        reversedDescription = "Deception, manipulation, all talk no action."))

    add(TarotCard("Page of Pentacles", ArcanaType.MINOR_COURT, suit = Suit.PENTACLES,
        baseDescription = "Manifestation, financial opportunity, skill development.",
        reversedDescription = "Lack of progress, procrastination, immaturity with money."))

    // ==================== KNIGHTS (4) ====================
    // 20° of sign to 20° of next sign

    add(TarotCard("Knight of Wands", ArcanaType.MINOR_COURT, suit = Suit.WANDS,
        longitudeRangeStart = 230.0, longitudeRangeEnd = 260.0, // 20° Scorpio → 20° Sagittarius
        baseDescription = "Adventure, impulsiveness, energy, passion, charm.",
        reversedDescription = "Recklessness, scattered energy, delays, frustration."))

    add(TarotCard("Knight of Cups", ArcanaType.MINOR_COURT, suit = Suit.CUPS,
        longitudeRangeStart = 320.0, longitudeRangeEnd = 350.0, // 20° Aquarius → 20° Pisces
        baseDescription = "Romance, charm, following the heart, imagination.",
        reversedDescription = "Moodiness, disappointment, unrealistic expectations."))

    add(TarotCard("Knight of Swords", ArcanaType.MINOR_COURT, suit = Suit.SWORDS,
        longitudeRangeStart = 50.0, longitudeRangeEnd = 80.0, // 20° Taurus → 20° Gemini
        baseDescription = "Ambition, action, drive, fast-thinking, assertive.",
        reversedDescription = "Impatience, impulsiveness, vicious communication."))

    add(TarotCard("Knight of Pentacles", ArcanaType.MINOR_COURT, suit = Suit.PENTACLES,
        longitudeRangeStart = 140.0, longitudeRangeEnd = 170.0, // 20° Leo → 20° Virgo
        baseDescription = "Hard work, routine, conservatism, methodical, reliable.",
        reversedDescription = "Laziness, boredom, feeling stuck, self-righteous."))

    // ==================== QUEENS (4) ====================

    add(TarotCard("Queen of Wands", ArcanaType.MINOR_COURT, suit = Suit.WANDS,
        longitudeRangeStart = 350.0, longitudeRangeEnd = 20.0, // 20° Pisces → 20° Aries (wraps 0°)
        baseDescription = "Courage, determination, independent, vibrancy, passionate.",
        reversedDescription = "Demanding, temperamental, jealous, insecure."))

    add(TarotCard("Queen of Cups", ArcanaType.MINOR_COURT, suit = Suit.CUPS,
        longitudeRangeStart = 80.0, longitudeRangeEnd = 110.0, // 20° Gemini → 20° Cancer
        baseDescription = "Compassion, calm, comfort, emotional security, intuition.",
        reversedDescription = "Martyr complex, insecurity, emotional manipulation."))

    add(TarotCard("Queen of Swords", ArcanaType.MINOR_COURT, suit = Suit.SWORDS,
        longitudeRangeStart = 170.0, longitudeRangeEnd = 200.0, // 20° Virgo → 20° Libra
        baseDescription = "Independent, unbiased, clear boundaries, direct communication.",
        reversedDescription = "Overly emotional, cold, manipulative."))

    add(TarotCard("Queen of Pentacles", ArcanaType.MINOR_COURT, suit = Suit.PENTACLES,
        longitudeRangeStart = 260.0, longitudeRangeEnd = 290.0, // 20° Sagittarius → 20° Capricorn
        baseDescription = "Practical, homebody, nurturing, financial security, motherly.",
        reversedDescription = "Imbalance between work and home, financial insecurity."))

    // ==================== KINGS (4) ====================

    add(TarotCard("King of Wands", ArcanaType.MINOR_COURT, suit = Suit.WANDS,
        longitudeRangeStart = 110.0, longitudeRangeEnd = 140.0, // 20° Cancer → 20° Leo
        baseDescription = "Natural leader, vision, entrepreneur, honour, bold.",
        reversedDescription = "Impulsive, overbearing, vain, ineffective leadership."))

    add(TarotCard("King of Cups", ArcanaType.MINOR_COURT, suit = Suit.CUPS,
        longitudeRangeStart = 200.0, longitudeRangeEnd = 230.0, // 20° Libra → 20° Scorpio
        baseDescription = "Emotionally balanced, compassionate, diplomatic, wise.",
        reversedDescription = "Emotional manipulation, moodiness, volatility."))

    add(TarotCard("King of Swords", ArcanaType.MINOR_COURT, suit = Suit.SWORDS,
        longitudeRangeStart = 290.0, longitudeRangeEnd = 320.0, // 20° Capricorn → 20° Aquarius
        baseDescription = "Authority, clear thinking, intellectual power, truth.",
        reversedDescription = "Manipulative, tyrannical, abusive use of power."))

    add(TarotCard("King of Pentacles", ArcanaType.MINOR_COURT, suit = Suit.PENTACLES,
        longitudeRangeStart = 20.0, longitudeRangeEnd = 50.0, // 20° Aries → 20° Taurus
        baseDescription = "Wealth, business acumen, leadership, security, abundance.",
        reversedDescription = "Authoritarian, corruption, financially irresponsible."))
}
