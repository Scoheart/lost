#!/bin/bash

# 项目清理脚本 - 删除已废弃的文件

echo "开始清理项目中的冗余文件..."

# 定义项目根目录
PROJECT_ROOT=../../

# 要删除的冗余文件列表
REDUNDANT_FILES=(
    # 已废弃的实体类 - 已被新的实体类替换
    "backend/src/main/java/com/community/lostandfound/entity/ForumPost.java"
    
    # 已废弃的DTO类 - 已被新的DTO类替换
    "backend/src/main/java/com/community/lostandfound/dto/comment/CommentDto.java"
    "backend/src/main/java/com/community/lostandfound/dto/comment/CommentPageDto.java"
    "backend/src/main/java/com/community/lostandfound/dto/comment/CreateCommentRequest.java"
)

# 删除文件
for file in "${REDUNDANT_FILES[@]}"; do
    full_path="$PROJECT_ROOT/$file"
    if [ -f "$full_path" ]; then
        echo "删除文件: $file"
        rm "$full_path"
    else
        echo "文件不存在，跳过: $file"
    fi
done

echo "冗余文件清理完成！"
echo "注意：以下文件虽然已废弃，但为了保持兼容性暂时保留："
echo " - backend/src/main/java/com/community/lostandfound/entity/Comment.java"
echo " - backend/src/main/java/com/community/lostandfound/service/CommentService.java"
echo " - backend/src/main/java/com/community/lostandfound/service/impl/CommentServiceImpl.java"
echo " - backend/src/main/java/com/community/lostandfound/controller/CommentController.java"
echo " - backend/src/main/java/com/community/lostandfound/repository/CommentRepository.java"
echo ""
echo "后续迁移完成后，可以删除这些文件。" 