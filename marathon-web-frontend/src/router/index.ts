import { createRouter, createWebHistory } from "vue-router";
//管理员
import AdminLogin from "@/pages/admin/AdminLogin.vue";
import AdminMain from "@/pages/admin/AdminMain.vue";
import AdminHealthAudit from "@/pages/admin/AdminHealthAudit.vue";
import AdminRaceApply from "@/pages/admin/AdminRaceApply.vue";
import AdminComment from "@/pages/admin/AdminComment.vue";
import AdminNews from "@/pages/admin/AdminNews.vue";
import AdminRules from "@/pages/admin/AdminRules.vue";

//举办方
import OrganizerLogin from "@/pages/organizer/OrgLogin.vue";
import OrganizerSignUp from "@/pages/organizer/OrgSignUp.vue";
import OrganizerMain from "@/pages/organizer/OrgMain.vue";
import OrganizerGrade from "@/pages/organizer/OrgGrade.vue";
import OrganizerDetail from "@/pages/organizer/OrgDetail.vue";
import OrganizerAthlete from "@/pages/organizer/OrgAthlete.vue";
import OrganizerComment from "@/pages/organizer/OrgComment.vue";
import OrganizerDraw from "@/pages/organizer/OrgDraw.vue";
import OrganizerRoute from "@/pages/organizer/OrgRoute.vue";

//公共
import Home from "@/pages/universal/Home.vue";
import RaceList from "@/pages/universal/RaceList.vue";
import Login from "@/pages/universal/Login.vue";
import RaceDetail from "@/pages/universal/RaceDetail.vue";
import Main from "@/pages/Main.vue";
import News from "@/pages/universal/News.vue";

//用户
import UserDetail from "@/pages/user/UserDetail.vue";
import UserEnroll from "@/pages/user/UserEnroll.vue";
import UserGrade from "@/pages/user/UserGrade.vue";
import Notice from "@/pages/user/Notice.vue";
import HealthDetail from "@/pages/user/HealthDetail.vue";
import HealthAudit from "@/pages/user/HealthAudit.vue";
import UserPayment from "@/pages/user/UserPayment.vue";
import SignUp from "@/pages/user/SignUp.vue";

const router = createRouter({
  //工作模式
  history: createWebHistory(),
  //路由规则 全小写
  routes: [
    {
      path: "/admin",
      component: AdminMain,
      redirect: "/admin/apply",
      children: [
        {
          path: "/admin/apply",
          component: AdminRaceApply,
        },
        {
          path: "/admin/audit",
          component: AdminHealthAudit,
        },
        {
          path: "/admin/comment",
          component: AdminComment,
        },
        {
          path: "/admin/news",
          component: AdminNews,
        },
        {
          path: "/admin/rules",
          component: AdminRules,
        }
      ]
    },
    {
      path: "/organizer",
      component: OrganizerMain,
      redirect: "/organizer/detail",
      children: [
        {
          path:"/organizer/detail",
          component: OrganizerDetail,
        },
        {
          path: "/organizer/grade",
          component: OrganizerGrade,
        },
        {
          path: "/organizer/athlete",
          component: OrganizerAthlete,
        },
        {
          path: "/organizer/comment",
          component: OrganizerComment,
        },
        {
          path: "/organizer/draw",
          component: OrganizerDraw,
        },
        {
          path: "/organizer/route",
          component: OrganizerRoute,
        }
      ]
    },
    {
      path: "/", component: Main,
      redirect: "/home",
      //子路由 在父组件中使用<router-view>渲染
      children: [
        {path: "/home", component: Home},
        { path: "/racelist", component: RaceList },
        {
          // 路由参数
          path: "/race/detail/:id",
          component: RaceDetail,
        },
        {
          path: "/user/payment/:raceId",
          component: UserPayment,
        },
        {
          path: "/user/detail",
          component: UserDetail,
        },
        {
          path: "/user/enroll",
          component: UserEnroll,
        },
        {
          path: "/user/grade",
          component: UserGrade,
        },
        {
          path: "/user/notice",
          component: Notice,
        },
        {
          path: "/user/health",
          component: HealthDetail,
        },
        {
          path: "/user/healthaudit",
          component: HealthAudit,
        },
        {
          path: "/login",
          component: Login,
        },
        {
          path: "/signup",
          component: SignUp,
        },
        {
          path: "/orglogin",
          component: OrganizerLogin,
        },
        {
          path: "/orgsignup",
          component: OrganizerSignUp,
        },
        {
          path: "/admlogin",
          component: AdminLogin,
        },
        {
          path: "/news",
          component: News,
        }
      ],
     },

  ],
});

// 全局前置守卫
router.beforeEach((to, from, next) => {
   // 判断是否是以 /user 开头的路由
  const isUserRoute = to.path.startsWith('/user');

  // 判断是否是以 /race 开头的路由
  const isRaceRoute = to.path.startsWith('/race/');

  // 判断是否是以 /organizer 开头的路由
  const isOrganizerRoute = to.path.startsWith('/organizer');

  // 判断是否是以 /admin 开头的路由
  const isAdminRoute = to.path.startsWith('/admin');


  //保护举办方
  if (isOrganizerRoute) {
    if (localStorage.getItem("type") === "2") {
      next();
    } else {
      next("/orglogin");
    }
  }
  // 如果举办方访问非 /organizer 开头的路由，重定向
  if (localStorage.getItem("type") === "2" &&!isOrganizerRoute) {
    next("/organizer/detail");
    return;
  }

  //保护管理员
  if (isAdminRoute) {
    if (localStorage.getItem("type") === "3") {
      next();
    } else {
      next("/admlogin");
    }
  }
  // 如果管理员访问非 /admin 开头的路由，重定向 
  if (localStorage.getItem("type") === "3" &&!isAdminRoute) {
    next("/admin/apply");
    return;
  }
  

  if (isUserRoute || isRaceRoute) {
    if (localStorage.getItem("token")) {
      next();
    } else {
      next("/login");
    }
  } else {
    next();
  }
});

export default router;
