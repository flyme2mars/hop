package com.flyme2mars.hop.data

/**
 * Local demo board only. No BLE, mesh, or radio.
 */
object FakeHopRepository {
    const val NearbyCount: Int = 7
    const val YouName: String = "You"

    fun seedPosts(): List<HopPost> = listOf(
        HopPost(
            id = 1,
            kind = PostKind.Offer,
            title = "Extra blanket, room 214",
            body = "Thick one from the cupboard. Knock if you are on this wing — I am up until 1.",
            author = "Priya",
            place = "214",
            postedAgo = "4 min",
        ),
        HopPost(
            id = 2,
            kind = PostKind.Ask,
            title = "USB-C charger for an hour?",
            body = "Phone is at 4%. Happy to sit in the lounge so you do not lose it.",
            author = "Dev",
            place = "Lounge",
            postedAgo = "12 min",
        ),
        HopPost(
            id = 3,
            kind = PostKind.Note,
            title = "Water on the 3rd stairwell",
            body = "Slow drip from the landing ceiling. Warden has been told; watch your step.",
            author = "Anika",
            place = "3rd stair",
            postedAgo = "18 min",
        ),
        HopPost(
            id = 4,
            kind = PostKind.Offer,
            title = "Dhaba run, two seats left",
            body = "Leaving from the gate in ten. Cash only, I can order if you hop a note.",
            author = "Rohan",
            place = "Gate",
            postedAgo = "25 min",
        ),
        HopPost(
            id = 5,
            kind = PostKind.Ask,
            title = "Lost keycard near laundry",
            body = "Blue sleeve, name Meera. If it turned up in a dryer, I owe you chai.",
            author = "Meera",
            place = "Laundry",
            postedAgo = "41 min",
        ),
        HopPost(
            id = 6,
            kind = PostKind.Note,
            title = "West wing power is back",
            body = "Lights and charging points on this side came up about ten minutes ago.",
            author = "Floor desk",
            place = "West wing",
            postedAgo = "1 hr",
        ),
        HopPost(
            id = 7,
            kind = PostKind.Offer,
            title = "Extra Maggi, room 108",
            body = "Two packs, kettle is on. Come before they turn to paste.",
            author = "Sam",
            place = "108",
            postedAgo = "2 hr",
        ),
        HopPost(
            id = 8,
            kind = PostKind.Ask,
            title = "Quiet after 11 on this corridor?",
            body = "Exam in the morning. A nod is enough — no need to police anyone.",
            author = "Jules",
            place = "Corridor B",
            postedAgo = "3 hr",
        ),
    )

    fun seedHistory(): List<HopPost> = listOf(
        HopPost(
            id = 101,
            kind = PostKind.Ask,
            title = "Spare pillow",
            body = "Claimed last night. Returned to 214 this morning.",
            author = "Dev",
            place = "214",
            postedAgo = "Yesterday",
            claimedBy = "Priya",
        ),
        HopPost(
            id = 102,
            kind = PostKind.Offer,
            title = "Power bank at the lounge",
            body = "Sat on the side table for anyone on 8%. Already back with Sam.",
            author = "Sam",
            place = "Lounge",
            postedAgo = "Yesterday",
            claimedBy = "Anika",
        ),
        HopPost(
            id = 103,
            kind = PostKind.Note,
            title = "Generator test at 6",
            body = "Lights flickered twice, then held. Desk marked it closed.",
            author = "Floor desk",
            place = "All floors",
            postedAgo = "Tue",
            claimedBy = "Floor desk",
        ),
    )
}
