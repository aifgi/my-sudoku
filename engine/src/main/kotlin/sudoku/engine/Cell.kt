package sudoku.engine

// Cell index convention: index = row * 9 + col  (0..80)

data class Cell(
    val index: Int,      // 0..80
    val digit: Int,      // 0 = empty, 1–9 = filled
    val isGiven: Boolean,
)

val Int.row: Int get() = this / 9
val Int.col: Int get() = this % 9
val Int.box: Int get() = (this / 9 / 3) * 3 + (this % 9 / 3)

fun peersOf(index: Int): IntArray {
    val peers = LinkedHashSet<Int>(20)
    val r = index.row
    val c = index.col
    val b = index.box
    for (i in 0..80) {
        if (i == index) continue
        if (i.row == r || i.col == c || i.box == b) {
            peers.add(i)
        }
    }
    return peers.toIntArray()
}

val PEERS: Array<IntArray> = Array(81) { peersOf(it) }
