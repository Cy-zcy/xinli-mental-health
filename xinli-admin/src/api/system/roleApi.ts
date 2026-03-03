import http from '@/utils/http'

// 角色列表分页
export const getRolePage = (params: any) => {
    return http.get({ url: '/api/admin/system/roles', params })
}

// 所有启用的角色
export const getAllRoles = () => {
    return http.get({ url: '/api/admin/system/roles/all' })
}

// 创建角色
export const createRole = (data: any) => {
    return http.post({ url: '/api/admin/system/roles', data })
}

// 更新角色
export const updateRole = (id: number, data: any) => {
    return http.put({ url: `/api/admin/system/roles/${id}`, data })
}

// 删除角色
export const deleteRole = (id: number) => {
    return http.del({ url: `/api/admin/system/roles/${id}` })
}

// 更改状态
export const updateRoleStatus = (id: number, status: number) => {
    return http.put({ url: `/api/admin/system/roles/${id}/status`, data: { status } })
}

// 获取角色权限 (返回 permissionCode 的数组)
export const getRolePermissions = (id: number) => {
    return http.get({ url: `/api/admin/system/roles/${id}/permissions` })
}

// 覆盖角色权限
export const saveRolePermissions = (id: number, permissionCodes: string[]) => {
    return http.post({ url: `/api/admin/system/roles/${id}/permissions`, data: permissionCodes })
}
