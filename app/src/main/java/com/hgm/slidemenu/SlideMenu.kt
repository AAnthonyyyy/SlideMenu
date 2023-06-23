package com.hgm.slidemenu

import android.app.Notification.Action
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Scroller
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.min

/**
 * @author：  HGM
 * @date：  2023-06-22 10:19
 */
@Suppress("UNREACHABLE_CODE")
class SlideMenu : ViewGroup, View.OnClickListener {
      //属性
      private val TAG = "SlideMenu"
      private var function = 0
      private lateinit var contentView: View
      private lateinit var actionsView: View
      private lateinit var scroller: Scroller//计算器，提供插值计算
      private var contentLeft = 0
      private var downX = 0f
      private var downY = 0f
      private var interceptDownX = 0f
      private var interceptDownY = 0f
      private var isOpen = false

      enum class Direction {
            LEFT, RIGHT, NONE
      }

      private var direction = Direction.NONE
      private val maxDuration = 800//固定滑动的时间
      private val minDuration = 300


      constructor(context: Context) : this(context, null, 0)

      constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

      constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context,
            attrs,
            defStyleAttr
      ) {
            initAttrs(context, attrs)
      }

      /**
       * 初始化属性
       */
      private fun initAttrs(context: Context, attrs: AttributeSet?) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.SlideMenu)
            function = a.getInt(R.styleable.SlideMenu_function, 0x30)
            a.recycle()
            scroller = Scroller(context)
      }


      /**
       * 添加子view
       */
      override fun onFinishInflate() {
            super.onFinishInflate()
            //只能有一个内容部分的子View
            if (childCount > 1) {
                  throw IllegalArgumentException("no more then one child")
            }
            contentView = getChildAt(0)

            //继续添加我们自己的子View（一共2个）
            actionsView =
                  LayoutInflater.from(context).inflate(R.layout.item_slide_menu_action, this, false)
            initActionsView()
            addView(actionsView)
      }

      /**
       * 初始化view
       */
      private fun initActionsView() {
            //设置action的点击事件
            actionsView.findViewById<TextView>(R.id.tv_read).setOnClickListener(this)
            actionsView.findViewById<TextView>(R.id.tv_top).setOnClickListener(this)
            actionsView.findViewById<TextView>(R.id.tv_delete).setOnClickListener(this)
      }


      /**
       * 测量
       */
      override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            //获取父控件宽高
            val widthSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSize = MeasureSpec.getSize(heightMeasureSpec)

            /*
                《测量第一个子View---内容部分》
                  宽度：和父控件一样宽
                  高度：1.指定大小，那直接获取它的大小
                             2.包裹内容，at_most，最大就那么大
                             3.撑满控件，那就给它大小
             */
            //获取它大小信息，判断高度的值
            val contentLayoutParams = contentView.layoutParams
            var contentHeightMeasureSpec = when (val contentHeight = contentLayoutParams.height) {
                  LayoutParams.MATCH_PARENT -> {
                        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY)
                  }

                  LayoutParams.WRAP_CONTENT -> {
                        MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST)
                  }

                  else -> {
                        MeasureSpec.makeMeasureSpec(contentHeight, MeasureSpec.EXACTLY)
                  }
            }
            contentView.measure(widthMeasureSpec, contentHeightMeasureSpec)
            //拿到内容部分测量完成后的高度
            val contentMeasuredHeight = contentView.measuredHeight

            /*
                《测量第二个子View---动作部分》
                  宽度：占3/4左右
                  高度：跟内容部分一致
             */
            val actionsWidth = widthSize * 3 / 4
            actionsView.measure(
                  MeasureSpec.makeMeasureSpec(actionsWidth, MeasureSpec.EXACTLY),
                  MeasureSpec.makeMeasureSpec(contentMeasuredHeight, MeasureSpec.EXACTLY)
            )

            /*
                《测量自己》
                  宽度：前面2个子View的总和
                  高度：和内容部分一致
             */
            setMeasuredDimension(widthSize + actionsWidth, contentMeasuredHeight)
      }


      /**
       * 布局
       */
      override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            /*
                  摆放内容部分
             */
            val contentTop = 0
            var contentRight = contentLeft + contentView.measuredWidth
            var contentBottom = contentTop + contentView.measuredHeight
            contentView.layout(contentLeft, contentTop, contentRight, contentBottom)

            /*
                  摆放动作部分
             */
            var actionsLeft = contentRight
            var actionsTop = contentTop
            var actionsRight = actionsLeft + actionsView.measuredWidth
            var actionsBottom = contentTop + actionsView.measuredHeight
            actionsView.layout(actionsLeft, actionsTop, actionsRight, actionsBottom)
      }


      /**
       * 定义接口
       */
      interface OnActionsClickListener {
            fun onReadClick()
            fun onTopClick()
            fun onDeleteClick()
      }

      private var onActionsClickListener: OnActionsClickListener? = null
      fun setOnActionsClickListener(listener: OnActionsClickListener) {
            onActionsClickListener = listener
      }

      override fun onClick(v: View?) {
            close()
            when (v?.id) {
                  R.id.tv_read -> {
                        onActionsClickListener?.onReadClick()
                  }

                  R.id.tv_top -> {
                        onActionsClickListener?.onTopClick()
                  }

                  R.id.tv_delete -> {
                        onActionsClickListener?.onDeleteClick()
                  }
            }
      }

      /**
       * 触摸监听
       */
      override fun onTouchEvent(event: MotionEvent?): Boolean {
            when (event?.action) {
                  MotionEvent.ACTION_DOWN -> {
                        downX = event.x
                        downY = event.y
                        Log.d(TAG, "ACTION_DOWN:$downX ")
                  }

                  MotionEvent.ACTION_MOVE -> {
                        val moveX = event.x
                        val moveY = event.y
                        //计算手指滑动的距离
                        val dx = (moveX - downX).toInt()//左滑：负数，右滑：正数
                        direction = if (dx > 0) {
                              Direction.RIGHT
                        } else {
                              Direction.LEFT
                        }


                        //计算总的手指滑动距离，边界问题
                        val resultScrollX = -dx + scrollX
                        //通过判断总的距离滑动view
                        if (resultScrollX <= 0) {
                              //左滑到底
                              scrollTo(0, 0)
                        } else if (resultScrollX > actionsView.measuredWidth) {
                              //右滑到底
                              scrollTo(actionsView.measuredWidth, 0)
                        } else {
                              //未满足
                              scrollBy(-dx, 0)
                        }
                        //view跟随手指滑动（2种方式）
                        //（1）scrollBy(-dx, 0)
                        //（2）contentTop+=dx
                        downX = moveX
                        downY = moveY
                        //Log.d(TAG, "ACTION_MOVE——>已经滑动的距离: $scrollX")
                  }

                  MotionEvent.ACTION_UP -> {
                        val hasBeenScrollX = scrollX//已经滑动的值
                        val actionsViewWidth = actionsView.measuredWidth//动作view的宽度

                        /*
                              两个重点：这里释放以后，是展开还是隐藏
                              1.是否已经展开
                              2.方向
                         */
                        if (isOpen) {
                              //当前状态打开
                              if (direction == Direction.RIGHT) {
                                    //右滑，小于4/5就关闭，否则打开
                                    if (hasBeenScrollX < actionsViewWidth * 4 / 5) {
                                          close()
                                    } else {
                                          open()
                                    }
                              } else if (direction == Direction.LEFT) {
                                    //左滑
                                    open()
                              }
                        } else {
                              //当前状态关闭
                              if (direction == Direction.LEFT) {
                                    //左滑，大于1/5就打开，否则关闭
                                    if (hasBeenScrollX > actionsViewWidth / 5) {
                                          open()
                                    } else {
                                          close()
                                    }
                              } else if (direction == Direction.RIGHT) {
                                    //右滑
                                    close()
                              }
                        }
                        //Log.d(TAG, "ACTION_UP——>已经滑动的距离: $scrollX")
                  }
            }
            return true
      }

      /**
       * 拦截事件
       */
      override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
            when (ev?.action) {
                  MotionEvent.ACTION_DOWN -> {
                        interceptDownX = ev.x
                        interceptDownY = ev.y
                  }

                  MotionEvent.ACTION_MOVE -> {
                        val x = ev.x
                        val y = ev.y
                        //大于0就说明在横向移动
                        if (abs(x - interceptDownX) > 0) {
                              //自己消费
                              return true
                        }
                  }
            }
            return super.onInterceptTouchEvent(ev)
      }

      /**
       * 展开
       */
      fun open() {
            /*
                  由于滑动的距离是根据用户已经滑出的距离计算得来，所以速度是不一致的，
                  为了视觉效果统一，要计算出每段距离的速度，滑动的时间不变。
                  公式：速度 = 滑行距离 / 总路程 * 时间
                           （总路程 = actionsView的宽度 * ？）（？= 规定的距离）
             */
            //展开
            val dx = actionsView.measuredWidth - scrollX
            val speed = dx / (actionsView.measuredWidth * 4 / 5f) * maxDuration
            var absSpeed = abs(speed.toInt())
            if (absSpeed < minDuration) {
                  absSpeed = minDuration
            }
            scroller.startScroll(scrollX, 0, dx, 0, absSpeed)
            isOpen = true
            invalidate()
            //Log.d(TAG, "open的速度：$absSpeed")
      }

      /**
       * 隐藏
       */
      fun close() {
            //隐藏
            val dx = -scrollX
            val speed = dx / (actionsView.measuredWidth * 4 / 5f) * maxDuration
            var absSpeed = abs(speed.toInt())
            if (absSpeed < minDuration) {
                  absSpeed = minDuration
            }
            scroller.startScroll(scrollX, 0, dx, 0, absSpeed)
            isOpen = false
            invalidate()
            //Log.d(TAG, "close的速度：$absSpeed")
      }


      override fun computeScroll() {
            if (scroller.computeScrollOffset()) {
                  val currX = scroller.currX
                  //滑动到指定位置即可
                  scrollTo(currX, 0)
                  //刷新ui
                  invalidate()
            }
      }
}