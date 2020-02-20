package org.ro.layout

import kotlinx.serialization.Serializable
import org.ro.to.TransferObject

/**
 * Parse layout specification.
 * Build UI Component tree.
 *
 * @See: https://en.wikipedia.org/wiki/Composite_pattern
 */
@Serializable
data class Layout(val cssClass: String? = null,
                  val row: List<Row> = emptyList()) : TransferObject {
    //TODO check if :TransferObject is required
    var properties = listOf<Property>()

    init {
        // row[0] (head) contains the object title and actions
        // row[1] contains data, tabs, collections, etc.
        val secondRow = row[1] // traditional C braintwist
        var colsLyt = secondRow.cols.first()
        var colLyt = colsLyt.getCol()
        val tgLyts = colLyt.tabGroup
        if (tgLyts.isNotEmpty()) {
            val tabGroup = tgLyts.first()
            val tab = tabGroup.tab.first()
            val row = tab.row.first()
            colsLyt = row.cols.first()
        }
        colLyt = colsLyt.getCol()
        val fsList = colLyt.fieldSet
        if (fsList!!.isNotEmpty()) {
            val fsLyt = fsList.first()
            properties = fsLyt.property
        }
    }

}
