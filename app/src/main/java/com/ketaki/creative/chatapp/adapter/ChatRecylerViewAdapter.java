package com.ketaki.creative.chatapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.ketaki.creative.chatapp.R;
import com.ketaki.creative.chatapp.models.ChatMessage;

import java.util.List;

/**
 * Created by gunke001 on 7/27/17.
 */

public class ChatRecylerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<ChatMessage> chatMessageList;
    private Context context;

    public ChatRecylerViewAdapter(List<ChatMessage> chats, Context context) {
        this.chatMessageList = chats;
        this.context = context;
    }

    public void add(ChatMessage chat) {
        chatMessageList.add(chat);
        notifyItemInserted(chatMessageList.size() - 1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder viewHolder = null;
        View view = layoutInflater.inflate(R.layout.message, parent, false);
        viewHolder = new ChatViewHolder(view,context);
        return viewHolder;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView, userTextView;
        private LinearLayout senderLayout;

        public ChatViewHolder(View itemView, Context context) {
            super(itemView);
             senderLayout = (LinearLayout) itemView.findViewById(R.id.sender_layout);
             messageTextView = new TextView(context);
             userTextView = new TextView(context);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ChatViewHolder chatViewHolder = (ChatViewHolder) holder;
        ChatMessage chat = chatMessageList.get(position);

        //set textviews with chatMessage and sender name
        chatViewHolder.messageTextView.setText(chat.getMessageText());
        chatViewHolder.userTextView.setText(chat.getMessageUser());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1.0f;

        //set sender text on left side and receiver text on left
        if (TextUtils.equals(chat.getMessageUser(), FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
            chatViewHolder.messageTextView.setBackgroundResource(R.drawable.bubble_in);
            layoutParams.gravity = Gravity.RIGHT;
        } else {
            layoutParams.gravity = Gravity.LEFT;
            chatViewHolder.messageTextView.setBackgroundResource(R.drawable.bubble_out);

        }
        if(chatViewHolder.messageTextView.getParent()!=null ){
            ((ViewGroup)chatViewHolder.messageTextView.getParent()).removeView(chatViewHolder.messageTextView);
        }
        if(chatViewHolder.userTextView.getParent()!=null ){
            ((ViewGroup)chatViewHolder.userTextView.getParent()).removeView(chatViewHolder.userTextView);
        }

        //add textviews to layout
        chatViewHolder.messageTextView.setLayoutParams(layoutParams);
        chatViewHolder.userTextView.setLayoutParams(layoutParams);
        chatViewHolder.senderLayout.addView(chatViewHolder.messageTextView);
        chatViewHolder.senderLayout.addView(chatViewHolder.userTextView);
    }

    @Override
    public int getItemCount() {
        if (chatMessageList != null) {
            return chatMessageList.size();
        }
        return 0;
    }
}
