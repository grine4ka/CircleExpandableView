package ru.bykov.circleexpandableview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import kotlin.math.*

class CircleExpandableView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val expandAnimator = ValueAnimator.ofFloat(MIN_SCALE_FACTOR, MAX_SCALE_FACTOR).apply {
        duration = DEFAULT_ANIMATION_DURATION
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            scaleFactor = it.animatedValue as Float
            invalidate()
        }
    }

    private val collapseAnimator = ValueAnimator.ofFloat(MAX_SCALE_FACTOR, MIN_SCALE_FACTOR).apply {
        duration = DEFAULT_ANIMATION_DURATION
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            scaleFactor = it.animatedValue as Float
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                expanded = false
            }

            override fun onAnimationCancel(animation: Animator?) {
                expanded = false
            }
        })
    }

    private val clockwiseRotationAnimator = ValueAnimator.ofFloat(0f, 45f).apply {
        duration = DEFAULT_ANIMATION_DURATION
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            rotationAngle = preservedRotation + it.animatedValue as Float
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                preservedRotation = rotationAngle
            }
        })
    }

    private val counterClockwiseRotationAnimator = ValueAnimator.ofFloat(0f, -45f).apply {
        duration = DEFAULT_ANIMATION_DURATION
        interpolator = DecelerateInterpolator()
        addUpdateListener {
            rotationAngle = preservedRotation + it.animatedValue as Float
            invalidate()
        }
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                preservedRotation = rotationAngle
            }
        })
    }

    private val circlePaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorCircle)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val selectedPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorCircle)
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    private val icon: Bitmap by lazy {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_solar)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        bitmap
    }

    private var selectedIndex: Int = NOT_SELECTED_INDEX
    private var expanded: Boolean = DEFAULT_EXPANDED
    private var nodeCount: Int = DEFAULT_NODES

    // draw values
    @FloatRange(from = 0.0, to = 1.0)
    private var scaleFactor: Float = MAX_SCALE_FACTOR
    private var rotationAngle: Float = 0f
    // touch values
    private var startAngle: Float = 0f
    private var preservedRotation: Float = 0f

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.CircleExpandableView, 0, 0
        )
        val nodeCount = a.getInteger(R.styleable.CircleExpandableView_cev_nodeCount, DEFAULT_NODES)
        val expanded = a.getBoolean(R.styleable.CircleExpandableView_cev_expanded, DEFAULT_EXPANDED)
        a.recycle()

        setNodeCount(nodeCount)
        if (expanded) expand() else collapse()
    }

    override fun onDraw(canvas: Canvas) {
        val centerX = (measuredWidth / 2).toFloat()
        val centerY = (measuredHeight / 2).toFloat()

        if (expanded) {
            drawExpanded(canvas, centerX, centerY)
        } else {
            drawCollapsed(canvas, centerX, centerY)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startAngle = getAngleInDegrees(event.x, event.y)
                preservedRotation = rotationAngle
            }
            MotionEvent.ACTION_MOVE -> {
                val currentAngle = getAngleInDegrees(event.x, event.y)
                val diff = (currentAngle - startAngle)
                rotationAngle = preservedRotation + diff
                invalidate()
            }
        }
        return true
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState =
            CircleExpandableSavedState(super.onSaveInstanceState())
        savedState.selectedIndex = selectedIndex
        savedState.expanded = expanded
        savedState.nodeCount = nodeCount
        savedState.scaleFactor = scaleFactor
        savedState.rotationAngle = rotationAngle
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is CircleExpandableSavedState) {
            selectedIndex = state.selectedIndex
            expanded = state.expanded
            nodeCount = state.nodeCount
            scaleFactor = state.scaleFactor
            rotationAngle = state.rotationAngle
            super.onRestoreInstanceState(state.superState)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun getAngleInDegrees(x: Float, y: Float): Float {
        val angleInRadians = atan2(x - measuredWidth / 2.toFloat(), height / 2 - y)
        return (angleInRadians * 180 / Math.PI).toFloat()
    }

    private fun drawCollapsed(canvas: Canvas, centerX: Float, centerY: Float) {
        val horizontalOffset = icon.width / 2
        val verticalOffset = icon.height / 2
        canvas.drawBitmap(
            icon,
            centerX - horizontalOffset,
            centerY - verticalOffset
        )
    }

    private fun drawExpanded(canvas: Canvas, centerX: Float, centerY: Float) {
        val horizontalOffset = icon.width / 2
        val verticalOffset = icon.height / 2
        val radiusOffset = max(horizontalOffset, verticalOffset)
        val radius = (min(measuredWidth / 2, measuredHeight / 2) - radiusOffset) * scaleFactor

        canvas.drawCircle(centerX, centerY, radius, circlePaint)
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(rotationAngle)
        val angle = Math.PI * 2 / nodeCount
        for (i in 0 until nodeCount) {
            if (i == selectedIndex) {
                canvas.drawBitmap(
                    icon,
                    radius * cos(angle * i).toFloat() - horizontalOffset,
                    radius * sin(angle * i).toFloat() - verticalOffset,
                    selectedPaint
                )
            } else {
                canvas.drawBitmap(
                    icon,
                    radius * cos(angle * i).toFloat() - horizontalOffset,
                    radius * sin(angle * i).toFloat() - verticalOffset
                )
            }
        }
        canvas.restore()
    }

    fun setSelectedIndex(selected: Int) {
        selectedIndex = if (selected < 0 || selected >= nodeCount) {
            NOT_SELECTED_INDEX
        } else {
            selected
        }
        invalidate()
    }

    fun setNodeCount(count: Int) {
        nodeCount = count
        invalidate()
    }

    fun getNodeCount(): Int {
        return nodeCount
    }

    fun isExpanded(): Boolean {
        return expanded
    }

    fun expand() {
        if (!expanded) {
            expanded = true
            expandAnimator.start()
        }
    }

    fun collapse() {
        if (expanded) {
            collapseAnimator.start()
        }
    }

    fun rotateClockwise() {
        clockwiseRotationAnimator.start()
    }

    fun rotateCounterClockwise() {
        counterClockwiseRotationAnimator.start()
    }

    companion object {
        const val DEFAULT_NODES = 3
        const val DEFAULT_EXPANDED = true
        const val NOT_SELECTED_INDEX = -1
        const val MAX_SCALE_FACTOR = 1f
        private const val MIN_SCALE_FACTOR = 0f
        private const val DEFAULT_ANIMATION_DURATION = 350L
    }

}

private fun Canvas.drawBitmap(bitmap: Bitmap, left: Float, top: Float) {
    drawBitmap(bitmap, left, top, null)
}