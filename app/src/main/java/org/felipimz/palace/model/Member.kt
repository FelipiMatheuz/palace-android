package org.felipimz.palace.model

data class Member(
    var id: String = "",
    var displayName: String = "",
    var status: Status = Status.OPEN
)
