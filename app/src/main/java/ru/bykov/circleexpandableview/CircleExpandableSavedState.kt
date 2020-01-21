package ru.bykov.circleexpandableview

import android.os.Parcel
import android.os.Parcelable
import android.view.View

internal class CircleExpandableSavedState : View.BaseSavedState {

    var selectedIndex: Int = CircleExpandableView.NOT_SELECTED_INDEX
    var expanded: Boolean = CircleExpandableView.DEFAULT_EXPANDED
    var nodeCount: Int = CircleExpandableView.DEFAULT_NODES
    var scaleFactor: Float = CircleExpandableView.MAX_SCALE_FACTOR
    var rotationAngle: Float = 0f

    constructor(parcelable: Parcelable?) : super(parcelable)

    constructor(parcel: Parcel) : super(parcel) {
        selectedIndex = parcel.readInt()
        expanded = parcel.readBooleanField()
        nodeCount = parcel.readInt()
        scaleFactor = parcel.readFloat()
        rotationAngle = parcel.readFloat()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeInt(selectedIndex)
        out.writeByte(if (expanded) 1 else 0)
        out.writeInt(nodeCount)
        out.writeFloat(scaleFactor)
        out.writeFloat(rotationAngle)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CircleExpandableSavedState> = object :
            Parcelable.Creator<CircleExpandableSavedState> {

            override fun createFromParcel(source: Parcel): CircleExpandableSavedState {
                return CircleExpandableSavedState(
                    source
                )
            }

            override fun newArray(size: Int): Array<CircleExpandableSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}

private fun Parcel.readBooleanField(): Boolean {
    return readByte().toInt() == 1
}
