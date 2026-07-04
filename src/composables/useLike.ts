import { ref } from 'vue'

/**
 * 点赞逻辑 composable
 * 首次点击 => likeCount +1, isLiked = true
 * 再次点击 => likeCount -1, isLiked = false
 */
export function useLike(
  initialLikes: number,
  initialLiked: boolean,
  likeApi: (id: number) => Promise<any>
) {
  const likes = ref(initialLikes)
  const isLiked = ref(initialLiked)

  async function toggleLike(id: number) {
    if (isLiked.value) {
      // 取消点赞
      likes.value--
      isLiked.value = false
    } else {
      // 点赞
      likes.value++
      isLiked.value = true
    }
    try {
      await likeApi(id)
    } catch {
      // 失败时回滚
      if (isLiked.value) {
        likes.value--
        isLiked.value = false
      } else {
        likes.value++
        isLiked.value = true
      }
    }
  }

  return { likes, isLiked, toggleLike }
}
