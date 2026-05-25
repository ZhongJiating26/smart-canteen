export function formatTime(dateStr) {
  if (!dateStr) return '--:--'
  const d = new Date(dateStr)
  let hours = d.getHours()
  const minutes = d.getMinutes().toString().padStart(2, '0')
  const ampm = hours >= 12 ? 'PM' : 'AM'
  hours = hours % 12 || 12
  return `${hours}:${minutes} ${ampm}`
}

export function formatDateTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
  return `${months[d.getMonth()]} ${d.getDate()}, ${formatTime(dateStr)}`
}

export function formatCurrency(amount) {
  return `$${Number(amount).toFixed(2)}`
}

export function formatElapsed(minutes) {
  if (minutes === undefined || minutes === null) return ''
  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes} min${minutes > 1 ? 's' : ''} ago`
  const hours = Math.floor(minutes / 60)
  return `${hours} hour${hours > 1 ? 's' : ''} ago`
}
