import { ref } from 'vue'

export function usePagination(defaultPageSize = 10) {
  const page = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)

  function resetPage() {
    page.value = 1
  }

  function setTotal(t: number) {
    total.value = t
  }

  function onPageChange(p: number) {
    page.value = p
  }

  function onPageSizeChange(ps: number) {
    pageSize.value = ps
    page.value = 1
  }

  return {
    page,
    pageSize,
    total,
    resetPage,
    setTotal,
    onPageChange,
    onPageSizeChange,
  }
}
