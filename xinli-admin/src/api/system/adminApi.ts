import http from '@/utils/http'

// 获取管理员列表
export const getAdminPage = (params: any) => {
    return http.get({ url: '/api/admin/system/admins', params })
}

// 创建新管理员
export const createAdmin = (data: any) => {
    return http.post({ url: '/api/admin/system/admins', data })
}

// 更新管理员
export const updateAdmin = (id: number, data: any) => {
    return http.put({ url: `/api/admin/system/admins/${id}`, data })
}

// 更改系统管理员状态
export const updateAdminStatus = (id: number, status: number) => {
    return http.put({ url: `/api/admin/system/admins/${id}/status`, data: { status } })
}

// 删除管理员
export const deleteAdmin = (id: number) => {
    return http.del({ url: `/api/admin/system/admins/${id}` })
}

// 重置密码
export const resetAdminPassword = (id: number, data: any) => {
    return http.put({ url: `/api/admin/system/admins/${id}/password`, data })
}
