package org.ro.ui.kv

import org.ro.ui.FormItem
import pl.treksoft.kvision.core.*
import pl.treksoft.kvision.form.FormPanel
import pl.treksoft.kvision.form.check.CheckBox
import pl.treksoft.kvision.form.formPanel
import pl.treksoft.kvision.form.select.SimpleSelect
import pl.treksoft.kvision.form.spinner.Spinner
import pl.treksoft.kvision.form.text.Password
import pl.treksoft.kvision.form.text.Text
import pl.treksoft.kvision.form.text.TextArea
import pl.treksoft.kvision.form.time.DateTime
import pl.treksoft.kvision.form.time.dateTime
import pl.treksoft.kvision.html.Div
import pl.treksoft.kvision.panel.SimplePanel
import pl.treksoft.kvision.panel.VPanel
import pl.treksoft.kvision.utils.px
import kotlin.js.Date

class FormPanelFactory(items: List<FormItem>) : VPanel() {

    var panel: FormPanel<String>?

    init {
        panel = formPanel {
            margin = 10.px
            for (fi: FormItem in items) {
                when (fi.type) {
                    "Text" -> add(createText(fi))
                    "Password" -> add(createPassword(fi))
                    "TextArea" -> add(createTextArea(fi))
                    "SimpleSelect" -> add(createSelect(fi))
                    "Html" -> add(createHtml(fi))
                    "Numeric" -> add(createNumeric(fi))
                    "Date" -> add(createDate(fi))
                    "Time" -> add(createTime(fi))
                    "Boolean" -> add(createBoolean(fi))
                }
            }
        }
    }

    private fun createBoolean(fi: FormItem): Component {
        if (fi.content is Boolean) {
            val item = CheckBox(label = fi.label, value = fi.content as Boolean)
            return item
        } else {
            return createText(fi)
        }
    }

    private fun createTime(fi: FormItem): DateTime {
        val item = dateTime(format = "HH:mm", label = fi.label, value = fi.content as Date)
        return item
    }

    private fun createDate(fi: FormItem): DateTime {
        val item = dateTime(
                format = "YYYY-MM-DD",
                label = fi.label,
                value = fi.content as Date
        )
        return item
    }

    private fun createNumeric(fi: FormItem): Spinner {
        val item = Spinner(label = fi.label, value = fi.content as Long)
        return item
    }

    private fun createHtml(fi: FormItem): Component {
        val item = SimplePanel()
        item.title = fi.label
        val border = Border(1.px, BorderStyle.GROOVE)
        item.border = border
        val div = Div(rich = true, content = fi.content.toString())
        div.padding = CssSize(15, UNIT.px)
        item.add(div)
        return item
    }

    private fun createText(fi: FormItem): Text {
        val item = Text(label = fi.label, value = fi.content.toString())
        item.readonly = fi.member?.isReadOnly()
        item.onEvent {
            change = {
                fi.changed(item.value)
                it.stopPropagation()
            }
        }
        return item
    }

    private fun createPassword(fi: FormItem): Password {
        return Password(label = fi.label, value = fi.content as String)
    }

    private fun createTextArea(fi: FormItem): TextArea {
        val rowCnt = maxOf(3, fi.size)
        val item = TextArea(label = fi.label, value = fi.content as String, rows = rowCnt)
        item.readonly = fi.readOnly
        item.onEvent {
            change = {
                fi.changed(item.value)
                it.stopPropagation()
            }
        }
        return item
    }

    private fun createSelect(fi: FormItem): SimpleSelect {
        @Suppress("UNCHECKED_CAST")
        val list = fi.content as List<StringPair>
        var preSelectedValue: String? = null
        if (list.isNotEmpty()) {
            preSelectedValue = list.first().first
        }
        return SimpleSelect(label = fi.label, options = list, value = preSelectedValue)
    }

}
