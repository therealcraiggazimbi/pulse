package com.gazimbi.pulse.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gazimbi.pulse.databinding.ItemParameterBinding
import com.gazimbi.pulse.models.Parameter

class ParameterAdapter(
    private val parameters: List<Parameter>,
    private val onItemClick: (Parameter) -> Unit
) : RecyclerView.Adapter<ParameterAdapter.ParameterViewHolder>() {

    inner class ParameterViewHolder(private val binding: ItemParameterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(parameter: Parameter) {
            // Uncomment and adjust as needed
            // binding.icon.setImageResource(parameter.icon)
            binding.name.text = parameter.name
            // binding.description.text = parameter.description
            binding.root.setOnClickListener { onItemClick(parameter) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParameterViewHolder {
        val binding = ItemParameterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ParameterViewHolder(binding)
    }

    override fun getItemCount() = parameters.size

    override fun onBindViewHolder(holder: ParameterViewHolder, position: Int) {
        holder.bind(parameters[position])
    }
}