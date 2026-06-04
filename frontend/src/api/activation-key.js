import request from '@/utils/request'

export const getActivationStatus = () => request.get('/activation-key/status')
export const verifyActivationKey = (data) => request.post('/activation-key/verify', data)
