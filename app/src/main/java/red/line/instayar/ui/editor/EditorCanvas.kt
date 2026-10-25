package red.line.instayar.ui.editor

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import red.line.instayar.data.model.BrandProfile
import red.line.instayar.data.model.Template
import red.line.instayar.data.model.TemplateElement
import red.line.instayar.ui.components.drawTemplate

@Composable
fun EditorInteractiveCanvas(
    template: Template,
    selectedElementId: String?,
    brandProfile: BrandProfile,
    onSelectElement: (String?) -> Unit,
    onDragElement: (dxRel: Float, dyRel: Float) -> Unit,
    onTransformElement: (scaleFactor: Float, deltaRotation: Float) -> Unit,
    onGestureEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appContext = LocalContext.current
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val maxW = maxWidth
        val maxH = maxHeight
        val aspect = template.aspectRatioFloat

        // Calculate size respecting container bounds
        val canvasModifier = if (maxW / maxH > aspect) {
            Modifier
                .aspectRatio(aspect)
                .fillMaxSize()
        } else {
            Modifier
                .aspectRatio(aspect)
                .fillMaxSize()
        }

        Box(
            modifier = canvasModifier
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0x887C3AED)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0F0B1E))
                .border(1.5.dp, Color(0x448B5CF6), RoundedCornerShape(16.dp))
                .testTag("editor_canvas_viewport")
        ) {
            // Interactive gesture layer
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(template.elements, selectedElementId) {
                        detectTapGestures { tapOffset ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            val relX = tapOffset.x / canvasW
                            val relY = tapOffset.y / canvasH

                            // Hit test in reverse order (top layer first)
                            val clickedElement = template.elements.reversed().find { el ->
                                relX in el.x..(el.x + el.width) &&
                                        relY in el.y..(el.y + el.height)
                            }
                            onSelectElement(clickedElement?.id)
                        }
                    }
                    .pointerInput(selectedElementId) {
                        detectTransformGestures { _, pan, zoom, rotation ->
                            val canvasW = size.width.toFloat()
                            val canvasH = size.height.toFloat()
                            if (canvasW > 0 && canvasH > 0) {
                                val dxRel = pan.x / canvasW
                                val dyRel = pan.y / canvasH

                                if (dxRel != 0f || dyRel != 0f) {
                                    onDragElement(dxRel, dyRel)
                                }
                                if (zoom != 1f || rotation != 0f) {
                                    onTransformElement(zoom, rotation)
                                }
                                onGestureEnd()
                            }
                        }
                    }
            ) {
                // 1. Draw Template content
                drawTemplate(
                    template = template,
                    canvasSize = size,
                    brandProfile = brandProfile,
                    context = appContext
                )

                // 2. Draw active selection bounding box if an element is selected
                selectedElementId?.let { id ->
                    val selectedEl = template.elements.find { it.id == id }
                    if (selectedEl != null) {
                        drawSelectionBox(selectedEl, size)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSelectionBox(
    element: TemplateElement,
    canvasSize: Size
) {
    val canvasW = canvasSize.width
    val canvasH = canvasSize.height

    val x = element.x * canvasW
    val y = element.y * canvasH
    val w = element.width * canvasW
    val h = element.height * canvasH
    val padding = 6f

    val cx = x + w / 2f
    val cy = y + h / 2f

    rotate(degrees = element.rotation, pivot = Offset(cx, cy)) {
        val rectTopLeft = Offset(x - padding, y - padding)
        val rectSize = Size(w + padding * 2f, h + padding * 2f)

        // Glowing selection frame
        drawRoundRect(
            color = Color(0xFF06B6D4),
            topLeft = rectTopLeft,
            size = rectSize,
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(
                width = 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        )

        // 4 Corner handles
        val handleRadius = 7f
        val corners = listOf(
            rectTopLeft,
            Offset(rectTopLeft.x + rectSize.width, rectTopLeft.y),
            Offset(rectTopLeft.x, rectTopLeft.y + rectSize.height),
            Offset(rectTopLeft.x + rectSize.width, rectTopLeft.y + rectSize.height)
        )

        for (corner in corners) {
            drawCircle(
                color = Color(0xFF06B6D4),
                radius = handleRadius,
                center = corner
            )
            drawCircle(
                color = Color.White,
                radius = handleRadius * 0.5f,
                center = corner
            )
        }

        // Top rotation stem & handle
        val stemTop = Offset(cx, rectTopLeft.y - 20f)
        drawLine(
            color = Color(0xFF06B6D4),
            start = Offset(cx, rectTopLeft.y),
            end = stemTop,
            strokeWidth = 2f
        )
        drawCircle(
            color = Color(0xFFEC4899),
            radius = handleRadius * 1.1f,
            center = stemTop
        )
    }
}
