package org.ro.ui.kv

import org.ro.core.Session
import org.ro.core.aggregator.BaseAggregator
import org.ro.core.aggregator.IAggregator
import org.ro.core.aggregator.ObjectAggregator
import org.ro.core.aggregator.UndefinedAggregator
import org.ro.core.event.EventStore
import org.ro.core.event.LogEntry
import org.ro.core.model.DisplayList
import org.ro.core.model.DisplayObject
import org.ro.to.TObject
import org.ro.to.mb.Menubars
import org.ro.ui.Point
import org.ro.ui.RoStatusBar
import org.w3c.dom.events.KeyboardEvent
import pl.treksoft.kvision.core.CssSize
import pl.treksoft.kvision.core.UNIT
import pl.treksoft.kvision.core.Widget
import pl.treksoft.kvision.dropdown.ContextMenu
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.utils.ESC_KEY
import kotlin.browser.window

/**
 * Single point of contact for view components consisting of:
 * @item RoMenubar,
 * @item RoView (tabs, etc.),
 * @item RoStatusbar,
 * @item Session
 */
object UiManager {

    private var session: Session? = null
    private val popups = mutableListOf<Widget>()

    init {
        window.addEventListener("keydown", fun(event) {
            val e = event as KeyboardEvent
            if (e.keyCode == ESC_KEY) {
                pop()
            }
        })
    }

    fun add(title: String, panel: SimplePanel, aggregator: IAggregator = UndefinedAggregator()) {
        RoView.addTab(title, panel)
        EventStore.addView(title, aggregator, panel)
    }

    fun closeView(tab: SimplePanel) {
        val tt = tab.title
        if (tt != null) {
            EventStore.closeView(tt)
        }
    }

    fun amendMenu(menuBars: Menubars) {
        RoMenuBar.amendMenu(menuBars)
    }

    fun updateStatus(entry: LogEntry) {
        RoStatusBar.update(entry)
    }

    fun updateUser(user: String) {
        RoStatusBar.updateUser(user)
    }

    fun openListView(aggregator: BaseAggregator) {
        val displayable = aggregator.dsp
        val title: String = displayable.extractTitle()
        val panel = RoTable(displayable as DisplayList)
        add(title, panel, aggregator)
        displayable.isRendered = true
    }

    fun openObjectView(aggregator: ObjectAggregator) {
        val displayable = aggregator.dsp
        var title: String = displayable.extractTitle()
        if (title.isEmpty()) {
            title = aggregator.actionTitle
        }
        val panel = RoDisplay(displayable as DisplayObject)
        add(title, panel, aggregator)
        displayable.isRendered = true
    }

    fun displayObject(tObject: TObject) {
        val aggregator = ObjectAggregator(tObject.title)
        linkLayout(tObject, aggregator)
        val logEntry = EventStore.find(tObject)!!
        logEntry.addAggregator(aggregator)
        aggregator.update(logEntry)
        console.log("[UiManager.displayObject]")
        console.log(logEntry)
        aggregator.handleObject(tObject)
    }

    private fun linkLayout(tObject: TObject, aggregator: ObjectAggregator) {
        val layoutLink = tObject.links.firstOrNull {
            it.rel.contains("object-layout")
        }
        val logEntry = EventStore.find(layoutLink!!.href)
        logEntry!!.addAggregator(aggregator)
    }

    fun openDialog(panel: RoDialog, at: Point = Point(100, 100)) {
        RoApp.add(panel)
        panel.left = CssSize(at.x, UNIT.px)
        panel.top = CssSize(at.x, UNIT.px)
        push(panel)
    }

    fun closeDialog(panel: RoDialog) {
        RoApp.remove(panel)
        pop()
    }

    fun getUrl(): String {
        return if (session == null) {
            ""
        } else {
            session!!.url
        }
    }

    fun login(url: String, username: String, password: String) {
        session = Session()
        session!!.login(url, username, password)
    }

    fun getCredentials(): String {
        return session!!.getCredentials()
    }

    fun push(widget: Widget) {
        popups.add(widget)
    }

    private fun pop() {
        val len = popups.size
        if (len > 0) {
            when (val widget = popups[len - 1]) {
                is RoDialog -> widget.close()
                is ContextMenu -> {
                    widget.hide()
                    widget.dispose()
                }
            }
            popups.removeAt(len - 1)
        }
    }

}
