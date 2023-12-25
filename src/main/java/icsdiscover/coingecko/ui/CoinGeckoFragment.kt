package icsdiscover.coingecko.ui

import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import icsdiscover.coingecko.R
import icsdiscover.coingecko.api.model.CoinGeckoTable
import icsdiscover.coingecko.databinding.FragmentCoingeckoBinding
import icsdiscover.coingecko.databinding.ItemCoingeckoBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private var coinGeckoViewModel: CoinGeckoViewModel? = null
    private var coinGeckoAdapter: CoinGeckoAdapter? = null

    companion object {
        var FILE_NAME = "crypto_watch"
        var ACTIVITY: Activity? = null
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
        coinGeckoViewModel = ViewModelProvider(this)[CoinGeckoViewModel::class.java]
        coinGeckoViewModel!!.liveData!!.observe(viewLifecycleOwner) {
            coinGeckoAdapter!!.submitList(it)
        }

        return root
    }

    fun reload() {
        coinGeckoViewModel?.refreshCoinGeckoList()
        ACTIVITY?.runOnUiThread {
            coinGeckoAdapter?.notifyDataSetChanged()
        }
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
        }) {

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
            holder.priceTextView!!.text =
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
                try {
                    val `in` = java.net.URL(imageURL).openStream()
                    val image = BitmapFactory.decodeStream(`in`)

                    imageView.setImageBitmap(image)
                }

                // If the URL does not point to
                // image or any other kind of failure
                catch (e: Exception) {
                    e.message?.let { Log.e("processImageURL", it) }
                }
            }
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