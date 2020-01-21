package ru.bykov.circleexpandableview

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

  private var expanded: Boolean = true
  private var nodeCount: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val widget = findViewById<CircleExpandableView>(R.id.multiiconView)
    nodeCount = widget.getNodeCount()
    expanded = widget.isExpanded()
    findViewById<Button>(R.id.addButton).setOnClickListener {
      widget.setNodeCount(++nodeCount)
    }
    findViewById<Button>(R.id.removeButton).setOnClickListener {
      if (nodeCount > 0) {
        widget.setNodeCount(--nodeCount)
      }
    }
    findViewById<Button>(R.id.expandButton).setOnClickListener {
      expanded = expanded.not()
      if (expanded) widget.expand() else widget.collapse()
    }
    findViewById<Button>(R.id.rotateClockwiseButton).setOnClickListener {
      widget.rotateClockwise()
    }
    findViewById<Button>(R.id.rotateCounterClockwiseButton).setOnClickListener {
      widget.rotateCounterClockwise()
    }
    findViewById<TextInputEditText>(R.id.selectedIndexEdit).setOnEditorActionListener { view, actionId, event ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        val selectedIndex = view.text.toString().toInt()
        widget.setSelectedIndex(selectedIndex)
        return@setOnEditorActionListener true
      }
      false
    }
  }
}
