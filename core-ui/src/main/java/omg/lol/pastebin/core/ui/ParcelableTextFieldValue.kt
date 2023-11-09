package omg.lol.pastebin.core.ui

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

/**
 * Parcelable wrapper for [TextFieldValue].
 *
 * __Note__: Only supports non-annotated text for now.
 *
 * @param wrappedValue [TextFieldValue] to wrap
 */
data class ParcelableTextFieldValue(val wrappedValue: TextFieldValue) : Parcelable {
    constructor(parcel: Parcel) : this(
        with(parcel) {
            TextFieldValue(
                text = readString()!!,
                selection = TextRange(start = readInt(), end = readInt())
            )
        }
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(wrappedValue) {
            parcel.writeString(text)
            parcel.writeInt(selection.start)
            parcel.writeInt(selection.end)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableTextFieldValue> {
        override fun createFromParcel(parcel: Parcel): ParcelableTextFieldValue {
            return ParcelableTextFieldValue(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableTextFieldValue?> {
            return arrayOfNulls(size)
        }
    }

}