package com.example.apriltagapp.model

data class Tag(
    /** 고유 태그 id */
    val id: Int = -1, // default value
    /** 연결된 태그 */
    val linkedTags: Array<LinkedTag> = arrayOf() // id, 가중치
) {
    /** 태그가 담고 있는 장소 */
    val spots: MutableList<Spot> = mutableListOf()

    fun addSpot(spot: Spot) {
        spots.add(spot)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Tag) {
            id == other.id
        } else false
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + linkedTags.contentHashCode()
        return result
    }
}