package com.example.apriltagapp.utility

import android.os.Parcel
import android.os.Parcelable

data class ParcelableArrivals(val transition: String, val destination: String):Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(transition)
        dest.writeString(destination)
    }

    companion object CREATOR : Parcelable.Creator<ParcelableArrivals> {
        override fun createFromParcel(parcel: Parcel): ParcelableArrivals {
            return ParcelableArrivals(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableArrivals?> {
            return arrayOfNulls(size)
        }
    }
}