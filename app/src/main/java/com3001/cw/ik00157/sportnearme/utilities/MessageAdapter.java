package com3001.cw.ik00157.sportnearme.utilities;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import com3001.cw.ik00157.sportnearme.R;
import com3001.cw.ik00157.sportnearme.models.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messagesList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Message> messagesList){
        this.messagesList = messagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView tvSenderMessage, tvReceiverMessage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            tvSenderMessage = itemView.findViewById(R.id.tv_sender_message);
            tvReceiverMessage = itemView.findViewById(R.id.tv_receiver_message);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Message message = messagesList.get(position);
        String fromUserId = message.getFrom();

        if(fromUserId.equals(messageSenderId)){
            // message from this user
            holder.tvReceiverMessage.setVisibility(View.INVISIBLE);
            holder.tvSenderMessage.setVisibility(View.VISIBLE);
            holder.tvSenderMessage.setBackgroundResource(R.drawable.sender_message_layout);
            holder.tvSenderMessage.setText(message.getMessage());
        } else{
            // message from other user
            holder.tvSenderMessage.setVisibility(View.INVISIBLE);
            holder.tvReceiverMessage.setVisibility(View.VISIBLE);
            holder.tvReceiverMessage.setBackgroundResource(R.drawable.receiver_message_layout);
            holder.tvReceiverMessage.setText(message.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

}
