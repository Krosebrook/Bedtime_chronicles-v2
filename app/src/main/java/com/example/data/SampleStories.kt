package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

data class StoryItem(
    val id: String,
    val title: String,
    val readTime: String,
    val category: String,
    val imageUrl: String,
    val isAiCrafted: Boolean = false,
    val isEditorsPick: Boolean = false
)

val sampleStories = listOf(
    StoryItem(
        id = "1",
        title = "The Starry Whales",
        readTime = "12 min",
        category = "Cosmic",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB1jLdKFdhpel1Kh5sjxf5LPi-lM2_IJkVTr3vU_S39m-NjISPDYMTdLYYVPsp3_scPtcgN0ZEfMckmFjCYwIN6gu3CKClELppPt-PwJlFfJwv6QTe7fUBtMmn5GNGgep8WrL7OKBM-Bri0iHWfxxCH5qKhVrUw7fwZ9PSzQ_s3xuWk1E-Fd7cU3yjwk8i37ZCOJwk712gYoQjBwx5VN3FKP0kA2UZXYWo7f_Eij0xQzUuxYc-zGK516RKEqOs5QZo6jRO7v2QOT7M",
        isEditorsPick = true
    ),
    StoryItem(
        id = "2",
        title = "The Candy Cloud Kingdom",
        readTime = "8 min",
        category = "Dreamy",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD2GS92MzDaZVymbMgwKOCZKTtbIVUYkfHQ1w5fTXKId1EPGnaozVj0VaDS1zxyzYCf04HhNG1tjaJYozBoaOcXAHeI8eTHUhYfuMqH82pKU7UNngPaoKM5rMz7H2o1yzdeoLMuhZBhHDAT8M5IH47OoXfUg-SRMUZxdanBa9ZY5WoiXLVSzxC0mIjnFwTnGZKXbQF7F3O_mj6ro4NrM-adLqhSjADoyPrZKJOZaFx1u-umCBTuIate5BXvjuYy5QxZJ5KNljFRYjI",
        isAiCrafted = true
    ),
    StoryItem(
        id = "3",
        title = "Journey to the Quiet Moon",
        readTime = "15 min",
        category = "Cosmic",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDtfI-7kVmM7mYeMB4oWZvFg5HRNksluebRPnvVXwEYg4t-E-z6WZbxwi91Qd488YuGHywpXf46ZlFYrumXXNCT5vY8ZZpULkGhkJ_grugOjkQx-uXES3SHW9xLcEmcJUgc-4OEZvmyv6oxoHnHG64P99AiTorTBvG-b5jtfjXgJWniclY3yJ0YD9y3o3UECbUOJnwRjYmcKs-DMdTgHgakILlhd_Zr96NEUr5qHtlRm9WFJ_i4rkidMBj6Y7sVcKv7wnU4ChJzSRI"
    ),
    StoryItem(
        id = "4",
        title = "The Firefly Symphony",
        readTime = "10 min",
        category = "Magical",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB-TjwLefFbFIWiGY08lsY9llMmP3KhFm1AYHidfGwvoZOJAEzXIphdIkIORdAaNEZ5fpI15-mONHWGEoLfV3PXbFROgoDvjFN-CWqIRtFfx4YccUU9iTmRKqnvzoPqCwsUl3HMYKkw-aARSgwzor_fa2JuUHdv25h51Rx5HrvTYH51xOEIEM6CP1q8MB-GS3JM2W25R72CRaEfBcvv9auvrNU0-QeE3gcXOgdOqAxA1ey1TOPlnHMOJVFYjT0huC6-8wyzyrKHwnw",
        isAiCrafted = true
    ),
    StoryItem(
        id = "5",
        title = "The Moonlit Rocket",
        readTime = "12 min",
        category = "Cosmic",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBD2EgYJNcTgQLMysSI-sqExHvtZCb4G7RLX6ZlDHskBWi2mdOw0bMqaVMfmoWfZN3LbOz_HiufCFpnXCK4flT3U5L6sxd0kRrV0luNagqP9nNMJK1_GW-essimN8zmJoonvNqYqkd4hD7Mrw3sWCvb1oz1C5Eu01qS5eoKAT7jfLOvSl7f43nfmB_L9eYy2aICo6kMc3DaqqY_SWnFDeyxnBSutbIq-6aBN7mV6EFcxlUlNjrU2XEkh1PkdnIa9GjSWa78Bu87wDc",
        isEditorsPick = true
    ),
    StoryItem(
        id = "6",
        title = "Whispering Woods",
        readTime = "8 min",
        category = "Magical",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAd1eODP8Ucs9hmxslITKW3Wb_6O5bUFBFpAw7GrZYBDU-b1cwubm7Yb3zcR0XPdtp-y4-dqb7e8EfW-BGz8yrTrsyoRYk9h2Q8Gow39erEzDksVCF6_KB4_5lzE1kSN22pV7NGbDHZpHFYyQ4Z21yXw-yN_m-nVcfpq9fHxgoLgaKI_9xovDhPdU1ZlwmNYG0lrzE30_3mYi7yP7Uu1en5iOpA9YxGJS9dCkqIsfzXISxSX_zkdoEGN1YtqWPZSyRRdtQ2IrE2bsg"
    ),
    StoryItem(
        id = "7",
        title = "Bear's Long Sleep",
        readTime = "15 min",
        category = "Winter",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAWYKv6VZ1thjcX3fim6gXMILVlP3J4zInl0pvdPXVmQCUizIyW8qIUOhiAXiVqxvxTb_N19n6z85wsgvJlOwiw0T4hkwBB0zyk1DdgL96RtAItsQGqsR7hZi5qQrjEj3JhKvqIiBH2HIorUxKNLmTkHAeVpqQUR6r4sOBLmh7gsXRQ5YdquHjPGfUFOutTaR3hVrC0jPeBmiQKqRkABX8Djj0D5QDyqGWSim6uraVfL09Sm28aj87qX33Ex_9-9chy7NEd4nE0nes"
    ),
    StoryItem(
        id = "8",
        title = "The Boy Who Caught a Star",
        readTime = "10 min",
        category = "Cosmic",
        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDHemjcFVQty3bEzUQBmwEpkrUu6d36cFYYIcnjAkSJ6Clv-fAOrZeY0zFKKNiL4BnSxLyc2L-8O3nW3wAzCwNXC_imd1QNVmtwL49VxYDtjMSwLUBuOOa8IHJC4BfhJNK6-BXoKbglomE15C2L2ut8xO5Uyjt3NaBPPxAioBUkXn6AYRYm3LsRcHKEuQz1us7Jkg3SOU521FBQr3LrvukMvHGa7uloGDUwAgCaKINJx3XPOZRl848UtarqXC3Y0pkDDv_Z4VonA-I",
        isAiCrafted = true
    )
)

val preloadedStoryContents = listOf(
    GeneratedStoryContent(
        id = "1",
        title = "The Starry Whales",
        category = "Cosmic",
        coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB1jLdKFdhpel1Kh5sjxf5LPi-lM2_IJkVTr3vU_S39m-NjISPDYMTdLYYVPsp3_scPtcgN0ZEfMckmFjCYwIN6gu3CKClELppPt-PwJlFfJwv6QTe7fUBtMmn5GNGgep8WrL7OKBM-Bri0iHWfxxCH5qKhVrUw7fwZ9PSzQ_s3xuWk1E-Fd7cU3yjwk8i37ZCOJwk712gYoQjBwx5VN3FKP0kA2UZXYWo7f_Eij0xQzUuxYc-zGK516RKEqOs5QZo6jRO7v2QOT7M",
        content = "Once upon a time, in the deepest part of the Cosmic Ocean, there swam two grand creatures known as the Starry Whales. Their bodies were made of glittering stardust and deep indigo energy, glowing gently as they glided through purple nebulae. They had a beautiful job: they sang the sweet lullabies of the cosmos to help children on Earth fall asleep.\n\nWhen they sang, their voices carried soft, soothing tones through the silent galaxy. Every twinkle of a star was a note in their song, a gentle reminder that you are safe, warm, and loved. As they traveled from constellation to constellation, they left a trail of golden dreamdust behind them.\n\nNow, as they dive into the quiet velvet dark of the night sky, they whisper: close your eyes, little explorer, and drift away into your own starry dreams."
    ),
    GeneratedStoryContent(
        id = "2",
        title = "The Candy Cloud Kingdom",
        category = "Dreamy",
        coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD2GS92MzDaZVymbMgwKOCZKTtbIVUYkfHQ1w5fTXKId1EPGnaozVj0VaDS1zxyzYCf04HhNG1tjaJYozBoaOcXAHeI8eTHUhYfuMqH82pKU7UNngPaoKM5rMz7H2o1yzdeoLMuhZBhHDAT8M5IH47OoXfUg-SRMUZxdanBa9ZY5WoiXLVSzxC0mIjnFwTnGZKXbQF7F3O_mj6ro4NrM-adLqhSjADoyPrZKJOZaFx1u-umCBTuIate5BXvjuYy5QxZJ5KNljFRYjI",
        content = "High above the sky, past the twilight moon, lies a magical place called the Candy Cloud Kingdom. Everything in this kingdom is made of soft, fluffy clouds that taste like sweet strawberries and warm vanilla. The trees are spun of pink cotton candy, and the rivers flow with gentle golden honey.\n\nThe citizens of this kingdom are tiny, winged cloud-kittens who float from bubble to bubble, playing quiet melodies on their silver harps. They keep the kingdom beautifully peaceful so that any child who dreams of it can rest in perfect comfort. When you visit this kingdom in your dreams, you can jump from one soft pink cloud to another, feeling lighter than a feather.\n\nEach cloud is warm, cozy, and perfectly supportive. Wrap yourself in your blanket now, and join the cloud-kittens as they play their softest sleepy song just for you."
    ),
    GeneratedStoryContent(
        id = "3",
        title = "Journey to the Quiet Moon",
        category = "Cosmic",
        coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDtfI-7kVmM7mYeMB4oWZvFg5HRNksluebRPnvVXwEYg4t-E-z6WZbxwi91Qd488YuGHywpXf46ZlFYrumXXNCT5vY8ZZpULkGhkJ_grugOjkQx-uXES3SHW9xLcEmcJUgc-4OEZvmyv6oxoHnHG64P99AiTorTBvG-b5jtfjXgJWniclY3yJ0YD9y3o3UECbUOJnwRjYmcKs-DMdTgHgakILlhd_Zr96NEUr5qHtlRm9WFJ_i4rkidMBj6Y7sVcKv7wnU4ChJzSRI",
        content = "A brave little traveler named Finn once embarked on a journey to the Quiet Moon. The Quiet Moon was unlike any other; it did not shine with blinding white light, but with a gentle, soothing silver glow. Upon reaching the moon, Finn met the Moon Guardian, a friendly, soft-furred creature wearing a golden crown.\n\nThe Moon Guardian showed Finn how the silver moonbeams are woven. Each moonbeam contains a little bit of quietness, designed to calm the busy thoughts of children on Earth. Finn helped the Guardian sprinkle the new stardust over the sleepy continents. The silver dust descended slowly, turning every bedroom into a warm, safe sanctuary.\n\nNow, the Moon Guardian invites you to rest your head and sleep under the watchful silver eye of the moon, assuring you that your dreams will be peaceful and safe."
    ),
    GeneratedStoryContent(
        id = "4",
        title = "The Firefly Symphony",
        category = "Magical",
        coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuB-TjwLefFbFIWiGY08lsY9llMmP3KhFm1AYHidfGwvoZOJAEzXIphdIkIORdAaNEZ5fpI15-mONHWGEoLfV3PXbFROgoDvjFN-CWqIRtFfx4YccUU9iTmRKqnvzoPqCwsUl3HMYKkw-aARSgwzor_fa2JuUHdv25h51Rx5HrvTYH51xOEIEM6CP1q8MB-GS3JM2W25R72CRaEfBcvv9auvrNU0-QeE3gcXOgdOqAxA1ey1TOPlnHMOJVFYjT0huC6-8wyzyrKHwnw",
        content = "Deep in the heart of the Enchanted Forest, as the sun dips below the horizon, a wonderful event begins. This is the nightly Firefly Symphony. Thousands of tiny fireflies gather in the forest clearing, each glowing with a warm, soft gold and emerald light.\n\nLed by a wise old owl, the fireflies blink in perfect rhythm, creating a mesmerizing dance of lights that looks like stars dancing on Earth. Their glowing wings hum a very soft, rhythmic song that sounds like the gentle rustle of leaves in a warm breeze.\n\nThe forest creatures lay down together on mossy beds, watching the gold symphony and letting the soothing harmony fill their hearts with quiet peace. You too can watch this beautiful light dance in your imagination, letting the warm, steady glow lull you into a safe and deep slumber."
    ),
    GeneratedStoryContent(
        id = "5",
        title = "The Moonlit Rocket",
        category = "Cosmic",
        coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuBD2EgYJNcTgQLMysSI-sqExHvtZCb4G7RLX6ZlDHskBWi2mdOw0bMqaVMfmoWfZN3LbOz_HiufCFpnXCK4flT3U5L6sxd0kRrV0luNagqP9nNMJK1_GW-essimN8zmJoonvNqYqkd4hD7Mrw3sWCvb1oz1C5Eu01qS5eoKAT7jfLOvSl7f43nfmB_L9eYy2aICo6kMc3DaqqY_SWnFDeyxnBSutbIq-6aBN7mV6EFcxlUlNjrU2XEkh1PkdnIa9GjSWa78Bu87wDc",
        content = "Close your eyes and climb aboard the Moonlit Rocket. This special ship is painted in deep blues and shiny silver, powered entirely by soft moonlight and gentle stardust. It runs completely silent, gliding through the dark sky without a sound.\n\nAs we leave the Earth behind, we travel past friendly satellites and glowing starlight lanes. The universe outside is vast and peaceful, like a dark velvet blanket studded with millions of sparkling diamonds. The cabin is incredibly warm, with soft pillows and a cozy blanket that fits you perfectly.\n\nWe orbit around a peaceful, quiet planet where sleepy moons sing a bedtime chorus. Let the gentle rocking motion of the rocket ship ease your mind, knowing that your journey through the stars is safe, and your destination is a night of sweet, deep sleep."
    ),
    GeneratedStoryContent(
        id = "6",
        title = "Whispering Woods",
        category = "Magical",
        coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAd1eODP8Ucs9hmxslITKW3Wb_6O5bUFBFpAw7GrZYBDU-b1cwubm7Yb3zcR0XPdtp-y4-dqb7e8EfW-BGz8yrTrsyoRYk9h2Q8Gow39erEzDksVCF6_KB4_5lzE1kSN22pV7NGbDHZpHFYyQ4Z21yXw-yN_m-nVcfpq9fHxgoLgaKI_9xovDhPdU1ZlwmNYG0lrzE30_3mYi7yP7Uu1en5iOpA9YxGJS9dCkqIsfzXISxSX_zkdoEGN1YtqWPZSyRRdtQ2IrE2bsg",
        content = "Welcome to the Whispering Woods, a gentle forest where even the tallest trees speak in soft, loving whispers. The leaves are made of velvet, rustling softly in the warm night wind. On the mossy ground, bioluminescent flowers glow with a warm lavender light, guiding your steps.\n\nIf you listen closely, the woods are telling you: 'You are safe, you are peaceful, you are wise.' Friendly forest animals, from sleepy squirrels to gentle deer, lie nestled under the protective branches. They all listen to the forest's comforting words, letting go of their busy day.\n\nDeep within the woods, a stream of clear blue water flows slowly over smooth pebbles, making a comforting, soft sound. Sleep now under the protection of the old, kind trees, and let the Whispering Woods fill your dreams with gentle peace."
    ),
    GeneratedStoryContent(
        id = "7",
        title = "Bear's Long Sleep",
        category = "Winter",
        coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuAWYKv6VZ1thjcX3fim6gXMILVlP3J4zInl0pvdPXVmQCUizIyW8qIUOhiAXiVqxvxTb_N19n6z85wsgvJlOwiw0T4hkwBB0zyk1DdgL96RtAItsQGqsR7hZi5qQrjEj3JhKvqIiBH2HIorUxKNLmTkHAeVpqQUR6r4sOBLmh7gsXRQ5YdquHjPGfUFOutTaR3hVrC0jPeBmiQKqRkABX8Djj0D5QDyqGWSim6uraVfL09Sm28aj87qX33Ex_9-9chy7NEd4nE0nes",
        content = "Deep in a warm, cozy cave hidden inside a snowy mountain, Barnaby Bear was preparing for his long winter sleep. He wore a special, soft woolen scarf and a round astronaut helmet, dreaming of floating among the stars.\n\nOut in the forest, the snow fell like tiny white feathers, covering the world in a silent blanket. Inside the cave, a small fire crackled softly, keeping Barnaby warm. He curled up on a bed of dry pine needles and soft green moss, pulling his heavy quilt up to his chin.\n\nBarnaby looked up at the cave ceiling, where tiny glowing crystals looked just like a mini constellation. He sighed a happy, sleepy sigh. The cold winter outside could not touch his warm sanctuary. Barnaby closed his eyes, took a deep breath, and began his long, beautiful sleep, dreaming of starfield adventures."
    ),
    GeneratedStoryContent(
        id = "8",
        title = "The Boy Who Caught a Star",
        category = "Cosmic",
        coverImageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuDHemjcFVQty3bEzUQBmwEpkrUu6d36cFYYIcnjAkSJ6Clv-fAOrZeY0zFKKNiL4BnSxLyc2L-8O3nW3wAzCwNXC_imd1QNVmtwL49VxYDtjMSwLUBuOOa8IHJC4BfhJNK6-BXoKbglomE15C2L2ut8xO5Uyjt3NaBPPxAioBUkXn6AYRYm3LsRcHKEuQz1us7Jkg3SOU521FBQr3LrvukMvHGa7uloGDUwAgCaKINJx3XPOZRl848UtarqXC3Y0pkDDv_Z4VonA-I",
        content = "Little Leo lived near the top of a quiet hill where the sky felt incredibly close. One night, a tiny star fell from its constellation and landed gently on his windowsill. It was a warm, golden, twinkling star, but its light was fading. Leo knew he had to help his new friend.\n\nHe built a small wooden balloon and sailed high into the night sky, past a family of glowing clouds. Leo returned the little star to its rightful place in the sky, right between two sleeping celestial dragons. As thanks, the star constellation shone with a gentle, comforting golden light, bathing the entire world below in a sleepy, warm glow.\n\nLeo sailed back down to his bed, feeling incredibly content, and fell asleep as his star friend twinkled a gentle 'goodnight' from the celestial sky."
    )
)
