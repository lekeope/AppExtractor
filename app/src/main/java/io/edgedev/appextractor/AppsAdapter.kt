package io.edgedev.appextractor

import android.support.v7.util.SortedList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

/**
 * Created by OPEYEMI OLORUNLEKE on 1/13/2018.
 */
class AppsAdapter(val clickedApp: ClickedApp, val comparator: Comparator<AppModel>) : RecyclerView.Adapter<AppsAdapter.AppHolder>() {
//    var list: MutableList<AppModel> = ArrayList<AppModel>()
    val mSortedList = SortedList<AppModel>(AppModel::class.java , MySortedListCallback())

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        val appModel = mSortedList[position]
        holder.imageView?.setImageDrawable(appModel.drawable)
        holder.nameTxtView?.text = appModel.appName
        holder.packagenameTxtView?.text = appModel.appPackageName
    }

    override fun getItemCount(): Int {
        return mSortedList.size()
    }

    fun replaceAll(models: List<AppModel>) {
        mSortedList.beginBatchedUpdates()
        for (i in mSortedList.size() - 1 downTo 0) {
            val model = mSortedList.get(i)
            if (!models.contains(model)) {
                mSortedList.remove(model)
            }
        }
        mSortedList.addAll(models)
        mSortedList.endBatchedUpdates()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.single_app_layout, parent, false)
        return AppHolder(view, clickedApp)
    }

    fun addApps(appModels: MutableList<AppModel>) {
//        list = appModels
        mSortedList.addAll(appModels)
    }

    inner class AppHolder(val view: View, val clickedApp: ClickedApp) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var imageView: ImageView? = null
        var nameTxtView: TextView? = null
        var packagenameTxtView: TextView? = null
        var extractButton: ImageButton? = null
        var infoButton: ImageButton? = null


        init {
            imageView = view.findViewById(R.id.app_icon)
            nameTxtView = view.findViewById(R.id.app_name)
            packagenameTxtView = view.findViewById(R.id.package_name)
            infoButton = view.findViewById(R.id.info)
            extractButton = view.findViewById(R.id.download)

            extractButton!!.setOnClickListener(this)
            infoButton!!.setOnClickListener(this)

        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.info -> clickedApp.onClickInfo(adapterPosition)
                R.id.download -> clickedApp.onClickDownload(adapterPosition)
            }
        }
    }

    inner class MySortedListCallback : SortedList.Callback<AppModel>() {
        override fun areItemsTheSame(item1: AppModel?, item2: AppModel?): Boolean {
            return item1?.id == item2?.id
        }

        override fun onInserted(position: Int, count: Int) {
            notifyItemRangeInserted(position, count)
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
            notifyItemMoved(fromPosition, toPosition)
        }

        override fun areContentsTheSame(oldItem: AppModel?, newItem: AppModel?): Boolean {
            return oldItem == newItem
        }

        override fun onChanged(position: Int, count: Int) {
            notifyItemRangeChanged(position, count)
        }

        override fun compare(app1: AppModel?, app2: AppModel?): Int {
            return comparator.compare(app1, app2)
        }

        override fun onRemoved(position: Int, count: Int) {
            notifyItemRangeRemoved(position, count)
        }

    }
}