package icsdiscover.coingecko.ui

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import icsdiscover.coingecko.R
import icsdiscover.coingecko.api.model.CoinGeckoTable
import icsdiscover.coingecko.databinding.FragmentCoingeckoBinding
import icsdiscover.coingecko.databinding.ItemCoingeckoBinding
import icsdiscover.coingecko.ui.CoinGeckoFragment.Companion.COIN_GECKO_VIEW_MODEL
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.URL
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale


/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
class CoinGeckoFragment : Fragment() {

    private var _binding: FragmentCoingeckoBinding? = null
    private var coinGeckoAdapter: CoinGeckoAdapter? = null

    companion object {
        var FILE_NAME = "crypto_watch"
        var ACTIVITY: Activity? = null
        var COIN_GECKO_VIEW_MODEL: CoinGeckoViewModel? = null
    }

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        FILE_NAME = activity?.title.toString().lowercase(Locale.ROOT).replace(" ", "_")
        ACTIVITY = requireActivity()

        _binding = FragmentCoingeckoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.recyclerviewCoingecko
        coinGeckoAdapter = CoinGeckoAdapter()
        recyclerView.adapter = coinGeckoAdapter
        COIN_GECKO_VIEW_MODEL = ViewModelProvider(this)[CoinGeckoViewModel::class.java]
        COIN_GECKO_VIEW_MODEL!!.liveData.observe(viewLifecycleOwner) {
            coinGeckoAdapter!!.submitList(it)
        }

        return root
    }

    fun reload() {
        COIN_GECKO_VIEW_MODEL?.refreshCoinGeckoList()
        ACTIVITY?.runOnUiThread {
            coinGeckoAdapter?.notifyDataSetChanged()
        }
    }

    fun setupSearch(item: MenuItem) {
        val searchView = item.actionView as SearchView?
        searchView!!.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                coinGeckoAdapter?.filter?.filter(newText)
                return false
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class CoinGeckoAdapter :
        ListAdapter<CoinGeckoTable, TransformViewHolder>(object :
            DiffUtil.ItemCallback<CoinGeckoTable>() {

            override fun areItemsTheSame(
                oldItem: CoinGeckoTable,
                newItem: CoinGeckoTable
            ): Boolean =
                oldItem == newItem

            override fun areContentsTheSame(
                oldItem: CoinGeckoTable,
                newItem: CoinGeckoTable
            ): Boolean =
                oldItem == newItem
        }), Filterable {

        val currencyFormat: NumberFormat = NumberFormat.getCurrencyInstance()
        val decimalFormat: DecimalFormat = DecimalFormat("#,###,###,##0.00")

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransformViewHolder {
            val binding = ItemCoingeckoBinding.inflate(LayoutInflater.from(parent.context))
            return TransformViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TransformViewHolder, position: Int) {
            val coinGeckoTable: CoinGeckoTable = getItem(position)
            val isDown = coinGeckoTable.price_change_percentage_24h < 0
            val upOrDown = if (isDown) "↓" else "↑"
            val textColor = if (isDown) Color.RED else holder.itemTextView.resources!!.getColor(
                R.color.teal_700,
                holder.itemTextView.context.theme
            )
            holder.itemTextView.text =
                coinGeckoTable.name + " (" + coinGeckoTable.symbol.uppercase() + ")"
            holder.priceTextView.text =
                "${currencyFormat.format(coinGeckoTable.current_price)} (${
                    currencyFormat.format(coinGeckoTable.price_change_24h)
                }) ${upOrDown}"
            holder.priceTextView.setTextColor(textColor)
            processImageURL(holder.imageView, coinGeckoTable.image)
            try {
                holder.hiloTextView!!.text =
                    "L: " + decimalFormat.format(coinGeckoTable.low_24h) + ", H: " + decimalFormat.format(
                        coinGeckoTable.high_24h
                    )
                holder.volumeTextView!!.text =
                    "Vol: " + decimalFormat.format(coinGeckoTable.total_volume)
            } catch (e: Exception) {
                Log.e("TransformAdapter", "Must be phone not tablet")
            }
        }

        private fun processImageURL(imageView: ImageView, imageURL: String) {
            GlobalScope.launch {
                // Tries to get the image and post it in the ImageView
                // with the help of Handler
                var `in`: InputStream? = null

                while (`in` == null) {
                    try {
                        `in` = URL(imageURL).openStream()
                        val image = BitmapFactory.decodeStream(`in`)

                        imageView.setImageBitmap(image)
                        break
                    }
                    // If the URL does not point to
                    // image or any other kind of failure
                    catch (e: Exception) {
                        e.message?.let { Log.e("processImageURL", it) }
                    }
                    delay(100)
                }
            }
        }

        override fun getFilter(): Filter {
            return CoinGeckoListFilter()
        }
    }

    class TransformViewHolder(binding: ItemCoingeckoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        val imageView: ImageView = binding.imageViewItem
        val itemTextView: TextView = binding.textViewItem
        val priceTextView: TextView = binding.textViewPrice
        val hiloTextView: TextView? = binding.textViewHilo
        val volumeTextView: TextView? = binding.textViewVolume
    }
}

class CoinGeckoListFilter : Filter() {
    private val coinGeckoTableList: ArrayList<CoinGeckoTable> =
        COIN_GECKO_VIEW_MODEL?.liveData?.value?.let { ArrayList<CoinGeckoTable>(it) }!!
    private val coinGeckoTableFullList: List<CoinGeckoTable>? =
        COIN_GECKO_VIEW_MODEL?.readFromCache()


    override fun performFiltering(constraint: CharSequence?): FilterResults {
        val filteredList: MutableList<CoinGeckoTable> = ArrayList<CoinGeckoTable>()
        val filterPattern: String =
            constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
        if (coinGeckoTableFullList != null) {
            for (item in coinGeckoTableFullList) {
                if (item.name.lowercase(Locale.ROOT)
                        .contains(filterPattern) || item.symbol.lowercase(
                        Locale.ROOT
                    ).contains(filterPattern)
                ) {
                    filteredList.add(item)
                }
            }
        }
        val results = FilterResults()
        results.values = filteredList
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        if (constraint.isNullOrEmpty()) {
            COIN_GECKO_VIEW_MODEL?.updateCoinGeckoList(coinGeckoTableFullList!!)
        } else {
            coinGeckoTableList.clear()
            if (results != null) {
                coinGeckoTableList.addAll(results.values as Collection<CoinGeckoTable>)
            }
            COIN_GECKO_VIEW_MODEL?.updateCoinGeckoList(coinGeckoTableList)
        }
    }

}
