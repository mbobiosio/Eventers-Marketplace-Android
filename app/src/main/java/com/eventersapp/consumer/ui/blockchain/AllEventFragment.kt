package com.eventersapp.consumer.ui.blockchain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.eventersapp.consumer.R
import com.eventersapp.consumer.databinding.FragmentAllEventBinding
import com.eventersapp.consumer.ui.adapter.CustomAdapterAllEvent
import com.eventersapp.consumer.ui.viewmodel.AllEventViewModel
import com.eventersapp.consumer.ui.viewmodelfactory.AllEventViewModelFactory
import com.eventersapp.consumer.util.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.closestKodein
import org.kodein.di.generic.instance

class AllEventFragment : Fragment(), KodeinAware {


    override val kodein by closestKodein()
    private lateinit var dataBind: FragmentAllEventBinding
    private lateinit var customAdapterAllEvent: CustomAdapterAllEvent
    private val factory: AllEventViewModelFactory by instance()
    private val viewModel: AllEventViewModel by lazy {
        ViewModelProvider(this, factory).get(AllEventViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customAdapterAllEvent = CustomAdapterAllEvent()
        getAllEvents()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dataBind = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_all_event,
            container,
            false
        )
        return dataBind.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupAPICall()
    }

    private fun setupUI() {
        val mLayoutManager = LinearLayoutManager(activity)
        mLayoutManager.reverseLayout = true
        mLayoutManager.stackFromEnd = true
        dataBind.recyclerViewAllEvent.apply {
            isNestedScrollingEnabled = false
            layoutManager = mLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = customAdapterAllEvent
        }
        dataBind.recyclerViewAllEvent.addOnItemTouchListener(
            RecyclerTouchListener(
                activity,
                dataBind.recyclerViewAllEvent,
                object : RecyclerTouchListener.ClickListener {

                    override fun onClick(view: View?, position: Int) {
                        findNavController().navigate(
                            R.id.action_dashboardFragment_to_buyEventFragment,
                            bundleOf("all_event_data" to customAdapterAllEvent.getListData()[position])
                        )
                    }

                    override fun onLongClick(view: View?, position: Int) {

                    }
                })
        )
    }


    private fun setupAPICall() {
        viewModel.allEventListLiveData.observe(viewLifecycleOwner, EventObserver { state ->
            when (state) {
                is State.Loading -> {
                    AppUtils.showProgressBar(requireContext())
                }
                is State.Success -> {
                    if (state.data.isNotEmpty()) {
                        dataBind.recyclerViewAllEvent.visibility = View.VISIBLE
                        dataBind.textClickOnPlus.visibility = View.GONE
                        customAdapterAllEvent.setData(state.data)
                    } else {
                        dataBind.recyclerViewAllEvent.visibility = View.GONE
                        dataBind.textClickOnPlus.visibility = View.VISIBLE
                    }
                    AppUtils.hideProgressBar()
                }
                is State.Error -> {
                    AppUtils.hideProgressBar()
                    requireActivity().showToast(state.message)
                }
            }
        })
    }


    private fun getAllEvents() {
        viewModel.getAllEventList()
    }

}
