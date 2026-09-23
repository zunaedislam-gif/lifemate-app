package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object QrCodeGenerator {

    /**
     * Generates a 2D QR Code bitmap representing text.
     * Uses a self-contained QR matrix generator.
     */
    fun createQrBitmap(content: String, size: Int = 512): Bitmap {
        val qrMatrix = SimpleQrEncoder.encode(content)
        val matrixSize = qrMatrix.size
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        val scale = size / matrixSize
        val offset = (size - (scale * matrixSize)) / 2

        // Fill background white
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, Color.WHITE)
            }
        }

        // Draw QR modules
        for (row in 0 until matrixSize) {
            for (col in 0 until matrixSize) {
                if (qrMatrix[row][col]) {
                    val startX = offset + col * scale
                    val startY = offset + row * scale
                    for (px in startX until (startX + scale)) {
                        for (py in startY until (startY + scale)) {
                            if (px in 0 until size && py in 0 until size) {
                                bitmap.setPixel(px, py, Color.BLACK)
                            }
                        }
                    }
                }
            }
        }

        return bitmap
    }
}

/**
 * Compact self-contained QR Code Version 2-3 Matrix generator
 * Supporting standard Finder patterns, Timing patterns, Alignment patterns, and data mask.
 */
object SimpleQrEncoder {
    fun encode(text: String): Array<BooleanArray> {
        val bytes = text.toByteArray(Charsets.UTF_8)
        // Choose size based on length (25x25 for v2, 29x29 for v3, 33x33 for v4)
        val dimension = when {
            bytes.size < 28 -> 25
            bytes.size < 48 -> 29
            bytes.size < 78 -> 33
            else -> 37
        }
        val grid = Array(dimension) { BooleanArray(dimension) { false } }
        val reserved = Array(dimension) { BooleanArray(dimension) { false } }

        fun placeFinderPattern(row: Int, col: Int) {
            for (r in 0 until 7) {
                for (c in 0 until 7) {
                    val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                    val isCenter = r in 2..4 && c in 2..4
                    grid[row + r][col + c] = isBorder || isCenter
                    reserved[row + r][col + c] = true
                }
            }
            // Separator spacing
            for (r in -1..7) {
                for (c in -1..7) {
                    val cr = row + r
                    val cc = col + c
                    if (cr in 0 until dimension && cc in 0 until dimension) {
                        reserved[cr][cc] = true
                    }
                }
            }
        }

        // Place 3 standard finder patterns
        placeFinderPattern(0, 0)
        placeFinderPattern(0, dimension - 7)
        placeFinderPattern(dimension - 7, 0)

        // Alignment pattern for dimension >= 25
        if (dimension >= 25) {
            val alignCenter = dimension - 7
            for (r in -2..2) {
                for (c in -2..2) {
                    val isBorder = kotlin.math.abs(r) == 2 || kotlin.math.abs(c) == 2
                    val isCenter = r == 0 && c == 0
                    val ar = alignCenter + r
                    val ac = alignCenter + c
                    if (ar in 0 until dimension && ac in 0 until dimension && !reserved[ar][ac]) {
                        grid[ar][ac] = isBorder || isCenter
                        reserved[ar][ac] = true
                    }
                }
            }
        }

        // Timing patterns
        for (i in 8 until dimension - 8) {
            grid[6][i] = (i % 2 == 0)
            reserved[6][i] = true
            grid[i][6] = (i % 2 == 0)
            reserved[i][6] = true
        }

        // Format info area reservation
        for (i in 0 until 9) {
            if (i < dimension) {
                reserved[8][i] = true
                reserved[i][8] = true
            }
        }
        for (i in 0 until 8) {
            reserved[8][dimension - 1 - i] = true
            reserved[dimension - 1 - i][8] = true
        }
        grid[dimension - 8][8] = true // Dark module

        // Encode data bits
        val bitBuffer = mutableListOf<Boolean>()
        // Mode indicator: 0100 (Byte mode)
        bitBuffer.addAll(listOf(false, true, false, false))
        // Character count (8 bits)
        val len = bytes.size.coerceAtMost(255)
        for (b in 7 downTo 0) {
            bitBuffer.add((len shr b and 1) == 1)
        }
        // Data bytes
        for (byte in bytes) {
            val ubyte = byte.toInt() and 0xFF
            for (b in 7 downTo 0) {
                bitBuffer.add((ubyte shr b and 1) == 1)
            }
        }
        // Terminator (up to 4 zeroes)
        repeat(4) { bitBuffer.add(false) }

        // Fill remaining capacity with pad bytes (0xEC, 0x11 alternating)
        var padToggle = true
        while (bitBuffer.size % 8 != 0) {
            bitBuffer.add(false)
        }
        val pad1 = listOf(true, true, true, false, true, true, false, false) // 0xEC
        val pad2 = listOf(false, false, false, true, false, false, false, true) // 0x11
        while (bitBuffer.size < (dimension * dimension / 2)) {
            bitBuffer.addAll(if (padToggle) pad1 else pad2)
            padToggle = !padToggle
        }

        // Place bits in zigzag columns
        var bitIndex = 0
        var up = true
        var col = dimension - 1
        while (col > 0) {
            if (col == 6) col-- // Skip vertical timing column
            val rows = if (up) (dimension - 1 downTo 0) else (0 until dimension)
            for (row in rows) {
                for (c in listOf(col, col - 1)) {
                    if (!reserved[row][c]) {
                        val bit = if (bitIndex < bitBuffer.size) bitBuffer[bitIndex++] else false
                        // Mask pattern 0: (row + col) % 2 == 0
                        val mask = ((row + c) % 2 == 0)
                        grid[row][c] = bit xor mask
                    }
                }
            }
            up = !up
            col -= 2
        }

        return grid
    }
}

@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val bitmap = remember(data) {
        QrCodeGenerator.createQrBitmap(data, 512)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Emergency Profile QR Code",
            modifier = Modifier.size(size - 16.dp)
        )
    }
}
