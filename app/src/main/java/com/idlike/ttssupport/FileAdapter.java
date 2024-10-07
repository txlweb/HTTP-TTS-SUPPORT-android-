package com.idlike.ttssupport;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    private final List<FileModel> fileList;
    private FileModel highlightedFileModel;
    public FileAdapter(List<FileModel> fileList) {
        this.fileList = fileList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FileModel file = fileList.get(position);
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(),
                file.equals(highlightedFileModel) ? R.color.purple_200 : R.color.white));
        holder.fileNameTextView.setText(file.getFileName());
        holder.fileSizeTextView.setText("  |  "+file.getFileSize() + " bytes");
        holder.imageView.setImageResource(R.drawable.unk_file);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                // 更新高亮状态
                highlightedFileModel = file;
                // 修改主Activity中选择的ID
                ModelMgrActivity.select_id = position;
                // 通知适配器数据发生变化，以便重新绑定视图
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        TextView fileSizeTextView;
        ImageView imageView;

        @SuppressLint("CutPasteId")
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.file_name_text_view);
            fileSizeTextView = itemView.findViewById(R.id.file_size_text_view);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}