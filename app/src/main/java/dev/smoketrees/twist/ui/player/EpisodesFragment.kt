package dev.smoketrees.twist.ui.player


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dev.smoketrees.twist.R
import dev.smoketrees.twist.adapters.EpisodeListAdapter
import dev.smoketrees.twist.model.twist.Result
import dev.smoketrees.twist.ui.home.AnimeViewModel
import dev.smoketrees.twist.utils.hide
import dev.smoketrees.twist.utils.show
import dev.smoketrees.twist.utils.toast
import kotlinx.android.synthetic.main.fragment_episodes.*
import org.koin.android.viewmodel.ext.android.sharedViewModel

class EpisodesFragment : Fragment() {

    private val args: EpisodesFragmentArgs by navArgs()
    private val viewModel by sharedViewModel<EpisodesViewModel>()
    private val animeViewModel by sharedViewModel<AnimeViewModel>()
//    private val slug = args.slugName!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_episodes, container, false)
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = EpisodeListAdapter(requireActivity()) { ep, shouldDownload ->
            val action = EpisodesFragmentDirections.actionEpisodesFragmentToAnimePlayerActivity(
                slugName = args.slugName,
                episodeNo = ep.number!!,
                shouldDownload = shouldDownload
            )
            findNavController().navigate(action)
        }
        val layoutManager = LinearLayoutManager(requireContext())
        episode_list.adapter = adapter
        episode_list.layoutManager = layoutManager


        viewModel.getAnimeDetails(args.slugName, args.id).observe(viewLifecycleOwner, Observer {
            when (it.status) {
                Result.Status.LOADING -> {
                    episode_spinkit.show()
                    episode_list.hide()
                    anime_image.hide()
                    anime_title.hide()
                    anime_rating.hide()
                    anime_episodes.hide()
                    anime_ongoing_text.hide()
                }

                Result.Status.SUCCESS -> {
                    it.data?.let { detailsEntity ->

                        anime_title.text = detailsEntity.title
                        anime_episodes.text = "${detailsEntity.episodeList.size} episodes"
                        anime_rating.text = "Score: ${detailsEntity.score}/10"
                        detailsEntity.airing?.let { ongoing ->
                            if (ongoing) anime_ongoing_text.show() else anime_ongoing_text.hide()
                        }
                        Glide.with(requireContext())
                            .load(detailsEntity.imageUrl)
                            .into(anime_image)

                        episode_spinkit.hide()
                        episode_list.show()
                        anime_image.show()
                        anime_title.show()
                        anime_rating.show()
                        anime_episodes.show()

                        if (detailsEntity.episodeList.isNotEmpty()) {
                            adapter.updateData(it.data.episodeList)
                        }
                    }
                }

                Result.Status.ERROR -> {
                    toast(it.message!!)
                    episode_list.hide()
                }
            }
        })
    }
}
