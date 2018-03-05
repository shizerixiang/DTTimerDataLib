package com.beviswang.datalibrary.util

import android.animation.Animator
import android.view.View
import android.view.ViewAnimationUtils

/**
 * 动画工具类
 * Created by shize on 2018/1/2.
 */
object AnimatorHelper {
    private val DIRECTION_CENTER = 0x101 // 中心
    private val DIRECTION_TOP_LEFT = 0x102 // 左上角
    private val DIRECTION_BOTTOM_RIGHT = 0x103 // 右下角

    /**
     * 启动扩散动画在中间
     *
     * @param view 需要执行动画的控件
     */
    fun startAnimationInCenter(view: View) {
        if (view.visibility == View.INVISIBLE) onVisibilityViewAnimator(view, DIRECTION_CENTER)
        else onGoneViewAnimator(view, DIRECTION_CENTER)
    }

    /**
     * 启动扩散动画在左上角
     *
     * @param view 需要执行动画的控件
     * @param marginX X 轴偏移量
     * @param marginY Y 轴偏移量
     */
    fun startAnimationInTopLeft(view: View, marginX: Int = 0, marginY: Int = 0) {
        if (view.visibility == View.INVISIBLE) onVisibilityViewAnimator(view, DIRECTION_TOP_LEFT, marginX, marginY)
        else onGoneViewAnimator(view, DIRECTION_TOP_LEFT, marginX, marginY)
    }

    /**
     * 启动扩散动画在右下角
     *
     * @param view 需要执行动画的控件
     * @param marginX X 轴偏移量
     * @param marginY Y 轴偏移量
     */
    fun startAnimationInBottomRight(view: View, marginX: Int = 0, marginY: Int = 0) {
        if (view.visibility == View.INVISIBLE) onVisibilityViewAnimator(view, DIRECTION_BOTTOM_RIGHT, marginX, marginY)
        else onGoneViewAnimator(view, DIRECTION_BOTTOM_RIGHT, marginX, marginY)
    }

    /**
     * 显示动画
     *
     * @param view
     * @param direction 方向
     * @param marginX 中心点 X 轴的偏移量
     * @param marginY 中心点 Y 轴的偏移量
     */
    private fun onVisibilityViewAnimator(view: View, direction: Int, marginX: Int = 0, marginY: Int = 0) {
        view.visibility = View.VISIBLE
        val animator: Animator
        when (direction) {
            DIRECTION_TOP_LEFT -> {
                animator = ViewAnimationUtils.createCircularReveal(view, 0 + marginX,
                        0 + marginY, 0f, getRadius(view, true))
                animator.duration = 400
            }
            DIRECTION_BOTTOM_RIGHT -> {
                animator = ViewAnimationUtils.createCircularReveal(view, view.width - marginX,
                        view.height - marginY, 0f, getRadius(view, true))
                animator.duration = 320
            }
            else -> {
                animator = ViewAnimationUtils.createCircularReveal(view, view.width / 2,
                        view.height / 2, 0f, getRadius(view, false))
                animator.duration = 320
            }
        }
        animator.start()
    }

    /**
     * 隐藏动画
     *
     * @param view
     * @param direction 方向
     * @param marginX 中心点 X 轴的偏移量
     * @param marginY 中心点 Y 轴的偏移量
     */
    private fun onGoneViewAnimator(view: View, direction: Int, marginX: Int = 0, marginY: Int = 0) {
        val animator: Animator
        when (direction) {
            DIRECTION_TOP_LEFT -> {
                animator = ViewAnimationUtils.createCircularReveal(view, 0 + marginX,
                        0 + marginY, getRadius(view, true), 0f)
                animator.duration = 320
            }
            DIRECTION_BOTTOM_RIGHT -> {
                animator = ViewAnimationUtils.createCircularReveal(view, view.width + marginX,
                        view.height + marginY, getRadius(view, true), 0f)
                animator.duration = 320
            }
            else -> {
                animator = ViewAnimationUtils.createCircularReveal(view, view.width / 2,
                        view.height / 2, getRadius(view, false), 0f)
                animator.duration = 320
            }
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {

            }

            override fun onAnimationEnd(animation: Animator?) {
                view.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {

            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })
        animator.start()
    }

    /**
     * 获取圆半径
     *
     * @param isCorner 是否为边角
     */
    private fun getRadius(view: View, isCorner: Boolean): Float = when {
        isCorner -> Math.sqrt(((view.width * view.width) + (view.height * view.height)).toDouble()).toFloat()
        view.height / 2 > view.width / 2 -> view.height / 2f
        else -> view.width / 2f
    }

}