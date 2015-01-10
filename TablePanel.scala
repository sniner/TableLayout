// A Scala Swing panel with TableLayout
//
// Author: Stefan Schönberger <mail@sniner.net>

package net.sniner.swing.utils

import net.sniner.swing.utils.TableLayout
import scala.swing._

class TablePanel(val format:String,
		 val rowPadding:Int = 0,
		 val colPadding:Int = 0) extends Panel with SequentialContainer.Wrapper {
    override lazy val peer = {
        new javax.swing.JPanel with SuperMixin {
            setLayout(new TableLayout(format, rowPadding, colPadding))
        }
    }
}
