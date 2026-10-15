package com.example.ui.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

class DragDropState(
    val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit,
    private val onDragCompleted: () -> Unit
) {
    var draggingItemIndex by mutableStateOf<Int?>(null)
        private set

    var draggingItemOffset by mutableFloatStateOf(0f)
        private set

    fun onDragStart(index: Int) {
        draggingItemIndex = index
        draggingItemOffset = 0f
    }

    fun onDrag(dragY: Float) {
        val currentIndex = draggingItemIndex ?: return
        draggingItemOffset += dragY

        val currentItemInfo = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == currentIndex } ?: return

        val startOffset = currentItemInfo.offset + draggingItemOffset
        val endOffset = startOffset + currentItemInfo.size
        val middleOffset = (startOffset + endOffset) / 2f

        val targetItem = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { item ->
            val itemRange = item.offset..(item.offset + item.size)
            middleOffset.toInt() in itemRange && item.index != currentIndex
        }

        if (targetItem != null) {
            val targetIndex = targetItem.index
            onMove(currentIndex, targetIndex)
            draggingItemOffset += (currentItemInfo.offset - targetItem.offset)
            draggingItemIndex = targetIndex
        }
    }

    fun onDragEnd() {
        val hadDragging = draggingItemIndex != null
        draggingItemIndex = null
        draggingItemOffset = 0f
        if (hadDragging) {
            onDragCompleted()
        }
    }

    fun onDragCancel() {
        draggingItemIndex = null
        draggingItemOffset = 0f
    }
}

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit,
    onDragCompleted: () -> Unit
): DragDropState {
    return remember(lazyListState, onMove, onDragCompleted) {
        DragDropState(lazyListState, onMove, onDragCompleted)
    }
}

fun Modifier.dragHandle(
    index: Int,
    dragDropState: DragDropState
): Modifier = pointerInput(dragDropState, index) {
    detectDragGestures(
        onDragStart = { dragDropState.onDragStart(index) },
        onDrag = { change, dragAmount ->
            change.consume()
            dragDropState.onDrag(dragAmount.y)
        },
        onDragEnd = { dragDropState.onDragEnd() },
        onDragCancel = { dragDropState.onDragCancel() }
    )
}
