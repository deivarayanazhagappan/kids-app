package com.deivarayan.kids.data

/**
 * Letter and digraph "sounds" for a simple phonics game. Android TTS cannot
 * pronounce raw isolated phonemes, so [PhonicsEntry.soundHint] is a written
 * approximation (e.g. "buh" for B) that the engine reads as a syllable -
 * the same trick most phonics apps use.
 */
object PhonicsData {

    fun phonicsFor(language: GameLanguage): List<PhonicsEntry> = when (language) {
        GameLanguage.ENGLISH -> english
        GameLanguage.GERMAN -> german
    }

    private fun p(grapheme: String, soundHint: String, word: String, emoji: String, imageQuery: String = word) =
        PhonicsEntry(grapheme, soundHint, word, emoji, imageQuery)

    private val english = listOf(
        // Single letters
        p("a", "a", "apple", "🍎"),
        p("b", "buh", "ball", "⚽"),
        p("c", "kuh", "cat", "🐱"),
        p("d", "duh", "dog", "🐶"),
        p("e", "eh", "egg", "🥚"),
        p("f", "fff", "fox", "🦊"),
        p("g", "guh", "goat", "🐐"),
        p("h", "huh", "hat", "🧢"),
        p("i", "ih", "ice cream", "🍦"),
        p("j", "juh", "jacket", "🧥"),
        p("k", "kuh", "kite", "🪁"),
        p("l", "lll", "lion", "🦁"),
        p("m", "mmm", "monkey", "🐵"),
        p("n", "nnn", "nose", "👃"),
        p("o", "oh", "orange", "🍊"),
        p("p", "puh", "pig", "🐷"),
        p("q", "kwuh", "queen", "👸"),
        p("r", "ruh", "rabbit", "🐰"),
        p("s", "sss", "sun", "☀️"),
        p("t", "tuh", "tiger", "🐯"),
        p("u", "uh", "umbrella", "☂️"),
        p("v", "vvv", "violin", "🎻"),
        p("w", "wuh", "whale", "🐳"),
        p("x", "ks", "box", "📦", "cardboard box"),
        p("y", "yuh", "yo-yo", "🪀"),
        p("z", "zzz", "zebra", "🦓"),
        // Common digraphs and blends
        p("sh", "sh", "ship", "🚢"),
        p("ch", "ch", "chicken", "🐔"),
        p("th", "th", "thumb", "👍"),
        p("ng", "ng", "king", "🤴"),
        p("ck", "kuh", "duck", "🦆"),
        p("ee", "ee", "bee", "🐝"),
        p("ea", "ee", "leaf", "🍃"),
        p("ai", "ay", "snail", "🐌"),
        p("ay", "ay", "play", "🎮", "children playing"),
        p("oa", "oh", "boat", "⛵"),
        p("ow", "oh", "snow", "❄️"),
        p("ou", "ow", "mouse", "🐭"),
        p("oi", "oy", "coin", "🪙"),
        p("oy", "oy", "toy", "🧸", "teddy bear toy"),
        p("oo", "oo", "moon", "🌙"),
        p("ar", "ar", "star", "⭐"),
        p("or", "or", "corn", "🌽"),
        p("er", "er", "bird", "🐦"),
        p("igh", "eye", "light", "💡"),
        p("ph", "fff", "phone", "📱"),
        p("wh", "wuh", "wheel", "🚲", "bicycle wheel"),
        p("qu", "kwuh", "quilt", "🧣", "quilt blanket"),
        // Consonant blends
        p("bl", "bl", "black", "⚫"),
        p("br", "br", "bread", "🍞"),
        p("cl", "cl", "clock", "🕐"),
        p("cr", "cr", "crab", "🦀"),
        p("dr", "dr", "drum", "🥁"),
        p("fl", "fl", "flower", "🌸"),
        p("fr", "fr", "frog", "🐸"),
        p("gl", "gl", "glasses", "👓"),
        p("gr", "gr", "grapes", "🍇"),
        p("pl", "pl", "plane", "✈️", "airplane"),
        p("pr", "pr", "present", "🎁", "gift"),
        p("sk", "sk", "skateboard", "🛹"),
        p("sl", "sl", "sleep", "😴"),
        p("sm", "sm", "smile", "🙂"),
        p("sn", "sn", "snake", "🐍"),
        p("sp", "sp", "spider", "🕷️"),
        p("st", "st", "star", "⭐"),
        p("sw", "sw", "swan", "🦢"),
        p("tr", "tr", "train", "🚂"),
        // More vowel and consonant patterns
        p("au", "aw", "dinosaur", "🦕"),
        p("ue", "oo", "blue", "🔵", "blue color"),
        p("ir", "er", "shirt", "👕", "t-shirt"),
        p("ur", "er", "turtle", "🐢"),
        p("dge", "j", "bridge", "🌉"),
        p("tch", "ch", "watch", "⌚"),
    )

    private val german = listOf(
        // Single letters
        p("a", "a", "Apfel", "🍎", "apple"),
        p("b", "b", "Ball", "⚽", "ball"),
        p("c", "k", "Computer", "💻", "computer"),
        p("d", "d", "Delfin", "🐬", "dolphin"),
        p("e", "e", "Elefant", "🐘", "elephant"),
        p("f", "f", "Fuchs", "🦊", "fox"),
        p("g", "g", "Giraffe", "🦒", "giraffe"),
        p("h", "h", "Hund", "🐶", "dog"),
        p("i", "i", "Insel", "🏝️", "island"),
        p("j", "j", "Jacke", "🧥", "jacket"),
        p("k", "k", "Katze", "🐱", "cat"),
        p("l", "l", "Löwe", "🦁", "lion"),
        p("m", "m", "Maus", "🐭", "mouse"),
        p("n", "n", "Nase", "👃", "nose"),
        p("o", "o", "Orange", "🍊", "orange"),
        p("p", "p", "Pilz", "🍄", "mushroom"),
        p("r", "r", "Rakete", "🚀", "rocket"),
        p("s", "s", "Sonne", "☀️", "sun"),
        p("t", "t", "Tiger", "🐯", "tiger"),
        p("u", "u", "Uhr", "🕐", "clock"),
        p("v", "f", "Vogel", "🐦", "bird"),
        p("w", "v", "Wal", "🐳", "whale"),
        p("z", "ts", "Zug", "🚂", "train"),
        // Common digraphs
        p("sch", "sch", "Schaf", "🐑", "sheep"),
        p("ch", "ch", "Milch", "🥛", "milk"),
        p("ck", "k", "Socken", "🧦", "socks"),
        p("ie", "ie", "Biene", "🐝", "bee"),
        p("ei", "ei", "Ei", "🥚", "egg"),
        p("eu", "eu", "Eule", "🦉", "owl"),
        p("ng", "ng", "Ring", "💍", "ring"),
        p("pf", "pf", "Pferd", "🐴", "horse"),
        p("qu", "kv", "Quadrat", "⬜", "square"),
        p("st", "scht", "Stern", "⭐", "star"),
        p("sp", "schp", "Spinne", "🕷️", "spider"),
        p("ß", "s", "Fuß", "🦶", "foot"),
        p("au", "au", "Haus", "🏠", "house"),
        p("chs", "ks", "Sechs", "6️⃣", "number six"),
        // Consonant clusters
        p("br", "br", "Brot", "🍞", "bread"),
        p("bl", "bl", "Blume", "🌸", "flower"),
        p("dr", "dr", "Drachen", "🪁", "kite"),
        p("fl", "fl", "Flagge", "🚩", "flag"),
        p("fr", "fr", "Frosch", "🐸", "frog"),
        p("kl", "kl", "Klavier", "🎹", "piano"),
        p("kr", "kr", "Krone", "👑", "crown"),
        p("schl", "schl", "Schlange", "🐍", "snake"),
        p("schm", "schm", "Schmetterling", "🦋", "butterfly"),
        p("schn", "schn", "Schnecke", "🐌", "snail"),
        p("schw", "schw", "Schwein", "🐷", "pig"),
        p("str", "str", "Strand", "🏖️", "beach"),
        p("tr", "tr", "Traktor", "🚜", "tractor"),
        p("zw", "zw", "Zwiebel", "🧅", "onion"),
    )
}
