package sudoku.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class CellVerifyTest {
    @Test
    fun peersSize() {
        assertEquals(20, PEERS[0].size)
        assertEquals(20, PEERS[40].size)
    }

    @Test
    fun allPeersSizeAndNoDuplicatesAndNoSelf() {
        for (idx in 0..80) {
            val p = PEERS[idx]
            assertEquals(20, p.size, "PEERS[$idx].size")
            assertFalse(idx in p.toSet(), "self in PEERS[$idx]")
            assertEquals(20, p.toSet().size, "duplicates in PEERS[$idx]")
        }
    }
}
