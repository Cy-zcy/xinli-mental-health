<template>
  <el-row :gutter="20" :style="{ marginTop: showWorkTab ? '0' : '10px' }" class="card-list">
    <el-col v-for="(item, index) in dataList" :key="index" :sm="12" :md="6" :lg="6">
      <div class="card art-custom-card">
        <span class="des subtitle">{{ item.des }}</span>
        <ArtCountTo class="number box-title" :target="item.num" :duration="1300" />
        <div class="change-box">
          <span class="change-text">较上周</span>
          <span
            class="change"
            :class="[item.change.indexOf('+') === -1 ? 'text-danger' : 'text-success']"
          >
            {{ item.change }}
          </span>
        </div>
        <i class="iconfont-sys" v-html="item.icon"></i>
      </div>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
  import { reactive, onMounted } from 'vue'
  import { storeToRefs } from 'pinia'
  import { useSettingStore } from '@/store/modules/setting'
  import { DashboardService } from '@/api/dashboardApi'
  import { ElMessage } from 'element-plus'
  import { handleApiResponse, getApiErrorMessage } from '@/utils/api'

  const { showWorkTab } = storeToRefs(useSettingStore())

  const dataList = reactive([
    {
      des: '总用户数',
      icon: '&#xe724;',
      startVal: 0,
      duration: 1000,
      num: 0,
      change: '+0%'
    },
    {
      des: '总帖子数',
      icon: '&#xe721;',
      startVal: 0,
      duration: 1000,
      num: 0,
      change: '+0%'
    },
    {
      des: '今日新用户',
      icon: '&#xe7aa;',
      startVal: 0,
      duration: 1000,
      num: 0,
      change: '+0%'
    },
    {
      des: '待审核帖子',
      icon: '&#xe82a;',
      startVal: 0,
      duration: 1000,
      num: 0,
      change: '+0%'
    }
  ])

  // 获取仪表板统计数据
  const loadDashboardStats = async () => {
    try {
      const response = await DashboardService.getDashboardStats()
      const stats = handleApiResponse(response)

      if (stats) {
        // 更新数据
        dataList[0].num = stats.totalUsers || 0
        dataList[1].num = stats.totalPosts || 0
        dataList[2].num = stats.todayNewUsers || 0
        dataList[3].num = stats.pendingPosts || 0

        // 这里可以根据需要计算变化百分比
        // 暂时设置为固定值，后续可以根据历史数据计算
        console.log('仪表板统计数据加载成功:', stats)
      }
    } catch (error: any) {
      console.error('获取仪表板数据失败:', error)
      ElMessage.error(getApiErrorMessage(error))
    }
  }

  // 页面加载时获取数据
  onMounted(() => {
    loadDashboardStats()
  })
</script>

<style lang="scss" scoped>
  .card-list {
    box-sizing: border-box;
    display: flex;
    flex-wrap: wrap;
    background-color: transparent !important;

    .art-custom-card {
      position: relative;
      box-sizing: border-box;
      display: flex;
      flex-direction: column;
      justify-content: center;
      width: 100%;
      height: 140px;
      padding: 0 18px;
      list-style: none;
      transition: all 0.3s ease;

      $icon-size: 52px;

      .iconfont-sys {
        position: absolute;
        top: 0;
        right: 20px;
        bottom: 0;
        width: $icon-size;
        height: $icon-size;
        margin: auto;
        overflow: hidden;
        font-size: 22px;
        line-height: $icon-size;
        color: var(--el-color-primary) !important;
        text-align: center;
        background-color: var(--el-color-primary-light-9);
        border-radius: 12px;
      }

      .des {
        display: block;
        height: 14px;
        font-size: 14px;
        line-height: 14px;
      }

      .number {
        display: block;
        margin-top: 10px;
        font-size: 28px;
        font-weight: 400;
      }

      .change-box {
        display: flex;
        align-items: center;
        margin-top: 10px;

        .change-text {
          display: block;
          font-size: 13px;
          color: var(--art-text-gray-600);
        }

        .change {
          display: block;
          margin-left: 5px;
          font-size: 13px;
          font-weight: bold;

          &.text-success {
            color: var(--el-color-success);
          }

          &.text-danger {
            color: var(--el-color-danger);
          }
        }
      }
    }
  }

  .dark {
    .card-list {
      .art-custom-card {
        .iconfont-sys {
          background-color: #232323 !important;
        }
      }
    }
  }
</style>
