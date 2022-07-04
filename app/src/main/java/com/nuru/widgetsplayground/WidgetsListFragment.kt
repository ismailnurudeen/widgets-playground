package com.nuru.widgetsplayground

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.SizeF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton

class WidgetListFragment : Fragment() {
    private val widgetViewModel: WidgetViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_app_list, container, false)

        view.findViewById<MaterialButton>(R.id.widget_button).apply {
            setOnClickListener {
                WidgetDialogFragment().show(childFragmentManager, WidgetDialogFragment.TAG)
            }
            setOnLongClickListener {
                widgetViewModel.unbind()
                true
            }
        }

        var widgetViews: List<AppWidgetHostView> = listOf()
        val widgetContainer = view.findViewById<LinearLayout>(R.id.widget)
        widgetContainer.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            widgetViews.forEach {
                it.updateAppWidgetSize(
                    widgetContainer.measuredWidth,
                    widgetContainer.context.pixelsToDips(
                        resources.getDimensionPixelSize(R.dimen.widget_max_height)
                    )
                )
            }
        }
        widgetViewModel.widgetHandles.observe(viewLifecycleOwner) {
            widgetContainer.apply {
                removeAllViews()
                widgetViews = it.map { widgetViewModel.getView(it) }
                widgetViews.forEachIndexed { index, hostView ->
                    val widgetView = FrameLayout(requireContext()).apply {
                        updatePadding(16,16,16,16)
                        addView(CardView(requireContext()).apply {
                            setContentPadding(8, 48, 8, 8)
                            radius = 28f
                            addView(hostView)
                        })
                    }
                    widgetView.setOnLongClickListener { _ ->
                        widgetViewModel.removeWidget(it[index])
                        Toast.makeText(requireContext(), "Widget Removed!", Toast.LENGTH_SHORT).show()
                        true
                    }
                    addView(widgetView)
                }
            }
        }
        return view
    }

    companion object {
        fun Context.pixelsToDips(pixels: Int): Int {
            return (pixels / resources.displayMetrics.density).toInt()
        }

        fun AppWidgetHostView.updateAppWidgetSize(maxWidthPixels: Int, maxHeightPixels: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                updateAppWidgetSize(
                    Bundle(),
                    listOf(SizeF(maxWidthPixels.toFloat(), maxHeightPixels.toFloat()))
                )
            } else {
                @Suppress("DEPRECATION")
                updateAppWidgetSize(Bundle(), 0, 0, maxWidthPixels, maxHeightPixels)
            }
        }
    }
}
