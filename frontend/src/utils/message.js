import { createVNode, render } from 'vue'
import MessageComponent from '@/components/MessageComponent.vue'

const messageContainer = document.createElement('div')
messageContainer.className = 'custom-message-container'
document.body.appendChild(messageContainer)

let messageInstances = []
let messageId = 0

export function showMessage(options) {
    const { type = 'info', message, duration = 3000 } = options

    const id = `message-${++messageId}`
    const instance = createVNode(MessageComponent, {
        id,
        type,
        message,
        duration,
        onClose: () => removeMessage(id)
    })

    const container = document.createElement('div')
    container.id = id
    messageContainer.appendChild(container)

    render(instance, container)
    messageInstances.push({ id, container })

    setTimeout(() => removeMessage(id), duration)
}

function removeMessage(id) {
    const index = messageInstances.findIndex(item => item.id === id)
    if (index > -1) {
        const { container } = messageInstances[index]
        render(null, container)
        container.remove()
        messageInstances.splice(index, 1)
    }
}

showMessage.success = (message, duration) => showMessage({ type: 'success', message, duration })
showMessage.error = (message, duration) => showMessage({ type: 'error', message, duration })
showMessage.warning = (message, duration) => showMessage({ type: 'warning', message, duration })
showMessage.info = (message, duration) => showMessage({ type: 'info', message, duration })

export default showMessage
