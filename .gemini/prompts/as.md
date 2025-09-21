# Prompt: Refactorizar para Consistencia Visual

## 1. CONFIGURACIÓN (PARA EL HUMANO)
*   **Propósito:** Este prompt instruye a la IA para analizar un componente React existente, compararlo con una referencia de HTML puro (de TailAdmin) y refactorizar el componente para que coincida visualmente a la perfección.
*   **Acción Requerida:** Edita las dos variables en la sección de ejecución: la ruta al componente a refactorizar y el código HTML de referencia.
*   **Modo de Uso:** Edita las variables, copia todo el contenido y pégalo en Gemini CLI.

---

## 2. TEMPLATE DE EJECUCIÓN (EDITAR ESTA SECCIÓN)

RUTA_DEL_COMPONENTE_A_REFACTORIZAR: "@src/components/MetricCard.tsx"

CODIGO_HTML_DE_REFERENCIA: """

<!doctype html>
<html lang="en">

        <!-- ===== Main Content Start ===== -->
        <main>
          <div class="mx-auto max-w-(--breakpoint-2xl) p-4 md:p-6">
            <div class="grid grid-cols-12 gap-4 md:gap-6">
              <div class="col-span-12">
                <!-- Metric Group Four -->
                <div class="grid grid-cols-1 gap-4 sm:grid-cols-2 md:gap-6 xl:grid-cols-3">
  <!-- Metric Item Start -->
  <div
    class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6"
  >
    <h4 class="text-title-sm font-bold text-gray-800 dark:text-white/90">
      $120,369
    </h4>

    <div class="mt-4 flex items-end justify-between sm:mt-5">
      <div>
        <p class="text-theme-sm text-gray-700 dark:text-gray-400">
          Active Deal
        </p>
      </div>

      <div class="flex items-center gap-1">
        <span
          class="flex items-center gap-1 rounded-full bg-success-50 px-2 py-0.5 text-theme-xs font-medium text-success-600 dark:bg-success-500/15 dark:text-success-500"
        >
          +20%
        </span>

        <span class="text-theme-xs text-gray-500 dark:text-gray-400">
          From last month
        </span>
      </div>
    </div>
  </div>
  <!-- Metric Item End -->

  <!-- Metric Item Start -->
  <div
    class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6"
  >
    <h4 class="text-title-sm font-bold text-gray-800 dark:text-white/90">
      $234,210
    </h4>

    <div class="mt-4 flex items-end justify-between sm:mt-5">
      <div>
        <p class="text-theme-sm text-gray-700 dark:text-gray-400">
          Revenue Total
        </p>
      </div>

      <div class="flex items-center gap-1">
        <span
          class="flex items-center gap-1 rounded-full bg-success-50 px-2 py-0.5 text-theme-xs font-medium text-success-600 dark:bg-success-500/15 dark:text-success-500"
        >
          +9.0%
        </span>

        <span class="text-theme-xs text-gray-500 dark:text-gray-400">
          From last month
        </span>
      </div>
    </div>
  </div>
  <!-- Metric Item End -->

  <!-- Metric Item Start -->
  <div
    class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] md:p-6"
  >
    <h4 class="text-title-sm font-bold text-gray-800 dark:text-white/90">
      874
    </h4>

    <div class="mt-4 flex items-end justify-between sm:mt-5">
      <div>
        <p class="text-theme-sm text-gray-700 dark:text-gray-400">
          Closed Deals
        </p>
      </div>

      <div class="flex items-center gap-1">
        <span
          class="flex items-center gap-1 rounded-full bg-error-50 px-2 py-0.5 text-theme-xs font-medium text-error-600 dark:bg-error-500/15 dark:text-error-500"
        >
          -4.5%
        </span>

        <span class="text-theme-xs text-gray-500 dark:text-gray-400">
          From last month
        </span>
      </div>
    </div>
  </div>
  <!-- Metric Item End -->
</div>
<!-- Metric Group Four -->
              </div>

              <div class="col-span-12 xl:col-span-8">
                <!-- ====== Chart Eleven Start -->
                <div
  class="rounded-2xl border border-gray-200 bg-white px-5 pb-5 pt-5 dark:border-gray-800 dark:bg-white/[0.03] sm:px-6 sm:pt-6"
>
  <div
    class="flex flex-col gap-5 mb-6 sm:flex-row sm:items-center sm:justify-between"
  >
    <div>
      <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
        Statistics
      </h3>
      <p class="mt-1 text-gray-500 text-theme-sm dark:text-gray-400">
        Target you’ve set for each month
      </p>
    </div>

    <div
      x-data="{selected: 'optionOne'}"
      class="inline-flex items-center gap-0.5 rounded-lg bg-gray-100 p-0.5 dark:bg-gray-900"
    >
      <button
        @click="selected = 'optionOne'"
        :class="selected === 'optionOne' ? 'shadow-theme-xs text-gray-900 dark:text-white bg-white dark:bg-gray-800' : 'text-gray-500 dark:text-gray-400'"
        class="px-3 py-2 font-medium rounded-md text-theme-sm hover:text-gray-900 hover:shadow-theme-xs dark:hover:bg-gray-800 dark:hover:text-white"
      >
        Monthly
      </button>

      <button
        @click="selected = 'optionTwo'"
        :class="selected === 'optionTwo' ? 'shadow-theme-xs text-gray-900 dark:text-white bg-white dark:bg-gray-800' : 'text-gray-500 dark:text-gray-400'"
        class="px-3 py-2 font-medium rounded-md text-theme-sm hover:text-gray-900 hover:shadow-theme-xs dark:hover:text-white"
      >
        Quarterly
      </button>

      <button
        @click="selected = 'optionThree'"
        :class="selected === 'optionThree' ? 'shadow-theme-xs text-gray-900 dark:text-white bg-white dark:bg-gray-800' : 'text-gray-500 dark:text-gray-400'"
        class="px-3 py-2 font-medium rounded-md text-theme-sm hover:text-gray-900 hover:shadow-theme-xs dark:hover:text-white"
      >
        Annually
      </button>
    </div>
  </div>

  <div class="flex gap-4 sm:gap-9">
    <div class="flex items-start gap-2">
      <div>
        <h4
          class="mb-0.5 text-base font-bold text-gray-800 dark:text-white/90 sm:text-theme-xl"
        >
          $212,142.12
        </h4>
        <span class="text-gray-500 text-theme-xs dark:text-gray-400">
          Avg. Yearly Profit
        </span>
      </div>

      <span
        class="mt-1.5 flex items-center gap-1 rounded-full bg-success-50 px-2 py-0.5 text-theme-xs font-medium text-success-600 dark:bg-success-500/15 dark:text-success-500"
      >
        +23.2%
      </span>
    </div>

    <div class="flex items-start gap-2">
      <div>
        <h4
          class="mb-0.5 text-base font-bold text-gray-800 dark:text-white/90 sm:text-theme-xl"
        >
          $30,321.23
        </h4>
        <span class="text-gray-500 text-theme-xs dark:text-gray-400">
          Avg. Yearly Profit
        </span>
      </div>

      <span
        class="mt-1.5 flex items-center gap-1 rounded-full bg-error-50 px-2 py-0.5 text-theme-xs font-medium text-error-600 dark:bg-error-500/15 dark:text-error-500"
      >
        -12.3%
      </span>
    </div>
  </div>
  <div class="max-w-full overflow-x-auto custom-scrollbar">
    <div id="chartEleven" class="-ml-4 min-w-[1000px] pl-2 xl:min-w-full"></div>
  </div>
</div>
<!-- ====== Chart Eleven End -->
              </div>

              <div class="col-span-12 xl:col-span-4">
                <!-- ====== Chart Twelve Start -->
                <div
  class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] sm:p-6"
>
  <div class="flex justify-between">
    <div>
      <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
        Estimated Revenue
      </h3>
      <p class="mt-1 text-theme-sm text-gray-500 dark:text-gray-400">
        Target you’ve set for each month
      </p>
    </div>

    <div x-data="{openDropDown: false}" class="relative h-fit">
      <button
        @click="openDropDown = !openDropDown"
        :class="openDropDown ? 'text-gray-700 dark:text-white' : 'text-gray-400 hover:text-gray-700 dark:hover:text-white'"
      >
        
      </button>
      <div
        x-show="openDropDown"
        @click.outside="openDropDown = false"
        class="absolute right-0 top-full z-40 w-40 space-y-1 rounded-2xl border border-gray-200 bg-white p-2 shadow-theme-lg dark:border-gray-800 dark:bg-gray-dark"
      >
        <button
          class="flex w-full rounded-lg px-3 py-2 text-left text-theme-xs font-medium text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
        >
          View More
        </button>
        <button
          class="flex w-full rounded-lg px-3 py-2 text-left text-theme-xs font-medium text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
        >
          Delete
        </button>
      </div>
    </div>
  </div>

  <div class="relative">
    <div id="chartTwelve"></div>
    <span
      class="absolute left-1/2 top-[60%] -translate-x-1/2 -translate-y-[60%] text-xs font-normal text-gray-500 dark:text-gray-400"
      >June Goals</span
    >
  </div>

  <div
    class="border-gary-200 mt-6 space-y-5 border-t pt-6 dark:border-gray-800"
  >
    <div>
      <p class="mb-2 text-theme-sm text-gray-500 dark:text-gray-400">
        Marketing
      </p>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div>
            <p class="text-base font-semibold text-gray-800 dark:text-white/90">
              $30,569.00
            </p>
          </div>
        </div>

        <div class="flex w-full max-w-[140px] items-center gap-3">
          <div
            class="relative block h-2 w-full max-w-[100px] rounded-sm bg-gray-200 dark:bg-gray-800"
          >
            <div
              class="absolute left-0 top-0 flex h-full w-[85%] items-center justify-center rounded-sm bg-brand-500 text-xs font-medium text-white"
            ></div>
          </div>
          <p class="text-theme-sm font-medium text-gray-700 dark:text-gray-400">
            85%
          </p>
        </div>
      </div>
    </div>

    <div>
      <p class="mb-2 text-theme-sm text-gray-500 dark:text-gray-400">Sales</p>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div>
            <p class="text-base font-semibold text-gray-800 dark:text-white/90">
              $20,486.00
            </p>
          </div>
        </div>

        <div class="flex w-full max-w-[140px] items-center gap-3">
          <div
            class="relative block h-2 w-full max-w-[100px] rounded-sm bg-gray-200 dark:bg-gray-800"
          >
            <div
              class="absolute left-0 top-0 flex h-full w-[55%] items-center justify-center rounded-sm bg-brand-500 text-xs font-medium text-white"
            ></div>
          </div>
          <p class="text-theme-sm font-medium text-gray-700 dark:text-gray-400">
            55%
          </p>
        </div>
      </div>
    </div>
  </div>
</div>
<!-- ====== Chart Twelve End -->
              </div>

              <div class="col-span-12 xl:col-span-6">
                <!-- ====== Chart Thirteen Start -->
                <div
  class="rounded-2xl border border-gray-200 bg-white p-5 sm:p-6 dark:border-gray-800 dark:bg-white/[0.03]"
>
  <div class="flex items-center justify-between mb-5">
    <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
      Sales Category
    </h3>

    <div x-data="{openDropDown: false}" class="relative">
      <button
        @click="openDropDown = !openDropDown"
        :class="openDropDown ? 'text-gray-700 dark:text-white' : 'text-gray-400 hover:text-gray-700 dark:hover:text-white'"
      >
        
      </button>
      <div
        x-show="openDropDown"
        @click.outside="openDropDown = false"
        class="absolute right-0 z-40 w-40 p-2 space-y-1 bg-white border border-gray-200 shadow-theme-lg dark:bg-gray-dark top-full rounded-2xl dark:border-gray-800"
      >
        <button
          class="flex w-full px-3 py-2 font-medium text-left text-gray-500 rounded-lg text-theme-xs hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
        >
          View More
        </button>
        <button
          class="flex w-full px-3 py-2 font-medium text-left text-gray-500 rounded-lg text-theme-xs hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
        >
          Delete
        </button>
      </div>
    </div>
  </div>
  <div class="flex flex-col items-center gap-8 xl:flex-row">
    <div id="chartThirteen" class="chartDarkStyle"></div>
    <div class="flex flex-col items-start gap-6 sm:flex-row xl:flex-col">
      <div class="flex items-start gap-2.5">
        <div class="bg-brand-500 mt-1.5 h-2 w-2 rounded-full"></div>
        <div>
          <h5
            class="mb-1 font-medium text-gray-800 text-theme-sm dark:text-white/90"
          >
            Affiliate Program
          </h5>
          <div class="flex items-center gap-2">
            <p
              class="font-medium text-gray-700 text-theme-sm dark:text-gray-400"
            >
              48%
            </p>
            <div class="w-1 h-1 bg-gray-400 rounded-full"></div>
            <p class="text-gray-500 text-theme-sm dark:text-gray-400">
              2,040 Products
            </p>
          </div>
        </div>
      </div>

      <div class="flex items-start gap-2.5">
        <div class="bg-brand-500 mt-1.5 h-2 w-2 rounded-full"></div>
        <div>
          <h5
            class="mb-1 font-medium text-gray-800 text-theme-sm dark:text-white/90"
          >
            Direct Buy
          </h5>
          <div class="flex items-center gap-2">
            <p
              class="font-medium text-gray-700 text-theme-sm dark:text-gray-400"
            >
              33%
            </p>
            <div class="w-1 h-1 bg-gray-400 rounded-full"></div>
            <p class="text-gray-400 text-theme-sm dark:text-gray-400">
              1,402 Products
            </p>
          </div>
        </div>
      </div>

      <div class="flex items-start gap-2.5">
        <div class="bg-brand-300 mt-1.5 h-2 w-2 rounded-full"></div>
        <div>
          <h5
            class="mb-1 font-medium text-gray-800 text-theme-sm dark:text-white/90"
          >
            Adsense
          </h5>
          <div class="flex items-center gap-2">
            <p
              class="font-medium text-gray-700 text-theme-sm dark:text-gray-400"
            >
              19%
            </p>
            <div class="w-1 h-1 bg-gray-400 rounded-full"></div>
            <p class="text-gray-500 text-theme-sm dark:text-gray-400">
              510 Products
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<!-- ====== Chart Thirteen End -->
              </div>

              <div class="col-span-12 xl:col-span-6">
                <!-- ====== Upcoming Schedule Start -->
                <div
  class="rounded-2xl border border-gray-200 bg-white p-5 dark:border-gray-800 dark:bg-white/[0.03] sm:p-6"
>
  <div class="mb-6 flex items-center justify-between">
    <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
      Upcoming Schedule
    </h3>

    <div x-data="{openDropDown: false}" class="relative">
      <button
        @click="openDropDown = !openDropDown"
        :class="openDropDown ? 'text-gray-700 dark:text-white' : 'text-gray-400 hover:text-gray-700 dark:hover:text-white'"
      >
        
      </button>
      <div
        x-show="openDropDown"
        @click.outside="openDropDown = false"
        class="absolute right-0 top-full z-40 w-40 space-y-1 rounded-2xl border border-gray-200 bg-white p-2 shadow-theme-lg dark:border-gray-800 dark:bg-gray-dark"
      >
        <button
          class="flex w-full rounded-lg px-3 py-2 text-left text-theme-xs font-medium text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
        >
          View More
        </button>
        <button
          class="flex w-full rounded-lg px-3 py-2 text-left text-theme-xs font-medium text-gray-500 hover:bg-gray-100 hover:text-gray-700 dark:text-gray-400 dark:hover:bg-white/5 dark:hover:text-gray-300"
        >
          Delete
        </button>
      </div>
    </div>
  </div>

  <div class="custom-scrollbar max-w-full overflow-x-auto">
    <div class="min-w-[500px]">
      <div class="flex flex-col gap-2">
        <div
          x-data="{checked: false}"
          @click="checked = !checked"
          class="flex cursor-pointer items-center gap-9 rounded-lg p-3 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-5 w-5 items-center justify-center rounded-md border-[1.25px]"
              :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
            >
              
            </div>
            <div>
              <span
                class="mb-0.5 block text-theme-xs text-gray-500 dark:text-gray-400"
              >
                Wed, 11 jan
              </span>
              <span
                class="text-theme-sm font-medium text-gray-700 dark:text-gray-400"
              >
                09:20 AM
              </span>
            </div>
          </div>
          <div>
            <span
              class="mb-1 block text-theme-sm font-medium text-gray-700 dark:text-gray-400"
            >
              Business Analytics Press
            </span>
            <span class="text-theme-xs text-gray-500 dark:text-gray-400">
              Exploring the Future of Data-Driven +6 more
            </span>
          </div>
        </div>

        <div
          x-data="{checked: false}"
          @click="checked = !checked"
          class="flex cursor-pointer items-center gap-9 rounded-lg p-3 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-5 w-5 items-center justify-center rounded-md border-[1.25px]"
              :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
            >
              
            </div>
            <div>
              <span
                class="mb-0.5 block text-theme-xs text-gray-500 dark:text-gray-400"
              >
                Fri, 15 feb
              </span>
              <span
                class="text-theme-sm font-medium text-gray-700 dark:text-gray-400"
              >
                10:35 AM
              </span>
            </div>
          </div>
          <div>
            <span
              class="mb-1 block text-theme-sm font-medium text-gray-700 dark:text-gray-400"
            >
              Business Sprint
            </span>
            <span class="text-theme-xs text-gray-500 dark:text-gray-400">
              Techniques from Business Sprint +2 more
            </span>
          </div>
        </div>

        <div
          x-data="{checked: false}"
          @click="checked = !checked"
          class="flex cursor-pointer items-center gap-9 rounded-lg p-3 hover:bg-gray-50 dark:hover:bg-white/[0.03]"
        >
          <div class="flex items-start gap-3">
            <div
              class="flex h-5 w-5 items-center justify-center rounded-md border-[1.25px]"
              :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
            >
              
            </div>
            <div>
              <span
                class="mb-0.5 block text-theme-xs text-gray-500 dark:text-gray-400"
              >
                Thu, 18 mar
              </span>
              <span
                class="text-theme-sm font-medium text-gray-700 dark:text-gray-400"
              >
                1:15 AM
              </span>
            </div>
          </div>
          <div>
            <span
              class="mb-1 block text-theme-sm font-medium text-gray-700 dark:text-gray-400"
            >
              Customer Review Meeting
            </span>
            <span class="text-theme-xs text-gray-500 dark:text-gray-400">
              Insights from the Customer Review Meeting +8 more
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<!-- ====== Upcoming Schedule End -->
              </div>

              <div class="col-span-12">
                <!-- Table Four -->
                <div
  class="overflow-hidden rounded-2xl border border-gray-200 bg-white pt-4 dark:border-gray-800 dark:bg-white/[0.03]"
>
  <div
    class="flex flex-col gap-5 px-6 mb-4 sm:flex-row sm:items-center sm:justify-between"
  >
    <div>
      <h3 class="text-lg font-semibold text-gray-800 dark:text-white/90">
        Recent Orders
      </h3>
    </div>

    <div class="flex flex-col gap-3 sm:flex-row sm:items-center">
      <form>
        <div class="relative">
          <span
            class="absolute -translate-y-1/2 pointer-events-none top-1/2 left-4"
          >
            
          </span>
          <input
            type="text"
            placeholder="Search..."
            class="dark:bg-dark-900 shadow-theme-xs focus:border-brand-300 focus:ring-brand-500/10 dark:focus:border-brand-800 h-10 w-full rounded-lg border border-gray-300 bg-transparent py-2.5 pr-4 pl-[42px] text-sm text-gray-800 placeholder:text-gray-400 focus:ring-3 focus:outline-hidden xl:w-[300px] dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30"
          />
        </div>
      </form>
      <div>
        <button
          class="text-theme-sm shadow-theme-xs inline-flex h-10 items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2.5 font-medium text-gray-700 hover:bg-gray-50 hover:text-gray-800 dark:border-gray-700 dark:bg-gray-800 dark:text-gray-400 dark:hover:bg-white/[0.03] dark:hover:text-gray-200"
        >
          

          Filter
        </button>
      </div>
    </div>
  </div>

  <div class="max-w-full overflow-x-auto custom-scrollbar">
    <table class="min-w-full">
      <!-- table header start -->
      <thead
        class="border-gray-100 border-y bg-gray-50 dark:border-gray-800 dark:bg-gray-900"
      >
        <tr>
          <th class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div x-data="{checked: false}" class="flex items-center gap-3">
                <div
                  @click="checked = !checked"
                  class="flex h-5 w-5 cursor-pointer items-center justify-center rounded-md border-[1.25px]"
                  :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
                >
                  
                </div>
                <div>
                  <span
                    class="block font-medium text-gray-500 text-theme-xs dark:text-gray-400"
                  >
                    Deal ID
                  </span>
                </div>
              </div>
            </div>
          </th>
          <th class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="font-medium text-gray-500 text-theme-xs dark:text-gray-400"
              >
                Customer
              </p>
            </div>
          </th>
          <th class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="font-medium text-gray-500 text-theme-xs dark:text-gray-400"
              >
                Product/Service
              </p>
            </div>
          </th>
          <th class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="font-medium text-gray-500 text-theme-xs dark:text-gray-400"
              >
                Deal Value
              </p>
            </div>
          </th>
          <th class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="font-medium text-gray-500 text-theme-xs dark:text-gray-400"
              >
                Close Date
              </p>
            </div>
          </th>
          <th class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="font-medium text-gray-500 text-theme-xs dark:text-gray-400"
              >
                Status
              </p>
            </div>
          </th>
          <th class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center justify-center">
              <p
                class="font-medium text-gray-500 text-theme-xs dark:text-gray-400"
              >
                Action
              </p>
            </div>
          </th>
        </tr>
      </thead>
      <!-- table header end -->

      <!-- table body start -->
      <tbody class="divide-y divide-gray-100 dark:divide-gray-800">
        <tr>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div x-data="{checked: false}" class="flex items-center gap-3">
                <div
                  @click="checked = !checked"
                  class="flex h-5 w-5 cursor-pointer items-center justify-center rounded-md border-[1.25px]"
                  :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
                >
                  
                </div>
                <div>
                  <span
                    class="block font-medium text-gray-700 text-theme-sm dark:text-gray-400"
                  >
                    DE124321
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div class="flex items-center gap-3">
                <div
                  class="flex items-center justify-center w-10 h-10 rounded-full bg-brand-100"
                >
                  <span class="text-xs font-semibold text-brand-500"> JD </span>
                </div>
                <div>
                  <span
                    class="text-theme-sm mb-0.5 block font-medium text-gray-700 dark:text-gray-400"
                  >
                    John Doe
                  </span>
                  <span class="text-gray-500 text-theme-sm dark:text-gray-400">
                    <a href="/cdn-cgi/l/email-protection" class="__cf_email__" data-cfemail="c6aca9aea8a2a3a986a1aba7afaae8a5a9ab">[email&#160;protected]</a>
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                Software License
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                $18,50.34
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                2024-06-15
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="bg-success-50 text-theme-xs text-success-600 dark:bg-success-500/15 dark:text-success-500 rounded-full px-2 py-0.5 font-medium"
              >
                Complete
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center justify-center">
              
            </div>
          </td>
        </tr>
        <tr>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div x-data="{checked: false}" class="flex items-center gap-3">
                <div
                  @click="checked = !checked"
                  class="flex h-5 w-5 cursor-pointer items-center justify-center rounded-md border-[1.25px]"
                  :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
                >
                  
                </div>
                <div>
                  <span
                    class="block font-medium text-gray-700 text-theme-sm dark:text-gray-400"
                  >
                    DE124321
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div class="flex items-center gap-3">
                <div
                  class="flex h-10 w-10 items-center justify-center rounded-full bg-[#fdf2fa]"
                >
                  <span class="text-xs font-semibold text-[#dd2590]"> KF </span>
                </div>

                <div>
                  <span
                    class="text-theme-sm mb-0.5 block font-medium text-gray-700 dark:text-gray-400"
                  >
                    Kierra Franci
                  </span>
                  <span class="text-gray-500 text-theme-sm dark:text-gray-400">
                    <a href="/cdn-cgi/l/email-protection" class="__cf_email__" data-cfemail="a8c3c1cddadac9e8cfc5c9c1c486cbc7c5">[email&#160;protected]</a>
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                Software License
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                $18,50.34
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                2024-06-15
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="bg-success-50 text-theme-xs text-success-600 dark:bg-success-500/15 dark:text-success-500 rounded-full px-2 py-0.5 font-medium"
              >
                Complete
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center justify-center">
              
            </div>
          </td>
        </tr>
        <tr>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div x-data="{checked: false}" class="flex items-center gap-3">
                <div
                  @click="checked = !checked"
                  class="flex h-5 w-5 cursor-pointer items-center justify-center rounded-md border-[1.25px]"
                  :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
                >
                  
                </div>
                <div>
                  <span
                    class="block font-medium text-gray-700 text-theme-sm dark:text-gray-400"
                  >
                    DE124321
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div class="flex items-center gap-3">
                <div
                  class="flex h-10 w-10 items-center justify-center rounded-full bg-[#f0f9ff]"
                >
                  <span class="text-xs font-semibold text-[#0086c9]"> EW </span>
                </div>

                <div>
                  <span
                    class="text-theme-sm mb-0.5 block font-medium text-gray-700 dark:text-gray-400"
                  >
                    Emerson Workman
                  </span>
                  <span class="text-gray-500 text-theme-sm dark:text-gray-400">
                    <a href="/cdn-cgi/l/email-protection" class="__cf_email__" data-cfemail="80e5ede5f2f3efeec0e7ede1e9ecaee3efed">[email&#160;protected]</a>
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                Software License
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                $18,50.34
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                2024-06-15
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="bg-warning-50 text-theme-xs text-warning-600 dark:bg-warning-500/15 dark:text-warning-400 rounded-full px-2 py-0.5 font-medium"
              >
                Pending
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center justify-center">
              
            </div>
          </td>
        </tr>
        <tr>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div x-data="{checked: false}" class="flex items-center gap-3">
                <div
                  @click="checked = !checked"
                  class="flex h-5 w-5 cursor-pointer items-center justify-center rounded-md border-[1.25px]"
                  :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
                >
                  
                </div>
                <div>
                  <span
                    class="block font-medium text-gray-700 text-theme-sm dark:text-gray-400"
                  >
                    DE124321
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div class="flex items-center gap-3">
                <div
                  class="flex h-10 w-10 items-center justify-center rounded-full bg-[#fff6ed]"
                >
                  <span class="text-xs font-semibold text-[#ec4a0a]"> CP </span>
                </div>

                <div>
                  <span
                    class="text-theme-sm mb-0.5 block font-medium text-gray-700 dark:text-gray-400"
                  >
                    Chance Philips
                  </span>
                  <span class="text-gray-500 text-theme-sm dark:text-gray-400">
                    <a href="/cdn-cgi/l/email-protection" class="__cf_email__" data-cfemail="72111a131c111732151f131b1e5c111d1f">[email&#160;protected]</a>
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                Software License
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                $18,50.34
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                2024-06-15
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="bg-success-50 text-theme-xs text-success-600 dark:bg-success-500/15 dark:text-success-500 rounded-full px-2 py-0.5 font-medium"
              >
                Complete
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center justify-center">
              
            </div>
          </td>
        </tr>
        <tr>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div x-data="{checked: false}" class="flex items-center gap-3">
                <div
                  @click="checked = !checked"
                  class="flex h-5 w-5 cursor-pointer items-center justify-center rounded-md border-[1.25px]"
                  :class="checked ? 'border-brand-500 dark:border-brand-500 bg-brand-500' : 'bg-white dark:bg-white/0 border-gray-300 dark:border-gray-700' "
                >
                  
                </div>
                <div>
                  <span
                    class="block font-medium text-gray-700 text-theme-sm dark:text-gray-400"
                  >
                    DE124321
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <div class="flex items-center gap-3">
                <div
                  class="flex items-center justify-center w-10 h-10 rounded-full bg-success-50"
                >
                  <span class="text-xs font-semibold text-success-600">
                    TG
                  </span>
                </div>

                <div>
                  <span
                    class="text-theme-sm mb-0.5 block font-medium text-gray-700 dark:text-gray-400"
                  >
                    Terry Geidt
                  </span>
                  <span class="text-gray-500 text-theme-sm dark:text-gray-400">
                    <a href="/cdn-cgi/l/email-protection" class="__cf_email__" data-cfemail="c6b2a3b4b4bf86a1aba7afaae8a5a9ab">[email&#160;protected]</a>
                  </span>
                </div>
              </div>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                Software License
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                $18,50.34
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p class="text-gray-700 text-theme-sm dark:text-gray-400">
                2024-06-15
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center">
              <p
                class="bg-success-50 text-theme-xs text-success-600 dark:bg-success-500/15 dark:text-success-500 rounded-full px-2 py-0.5 font-medium"
              >
                Complete
              </p>
            </div>
          </td>
          <td class="px-6 py-3 whitespace-nowrap">
            <div class="flex items-center justify-center">
              
            </div>
          </td>
        </tr>
      </tbody>
      <!-- table body end -->
    </table>
  </div>
</div>
<!-- Table Four -->
              </div>
            </div>
          </div>
        </main>
        <!-- ===== Main Content End ===== -->
      </div>
      <!-- ===== Content Area End ===== -->
    </div>
    <!-- ===== Page Wrapper End ===== -->
  <script data-cfasync="false" src="/cdn-cgi/scripts/5c5dd728/cloudflare-static/email-decode.min.js"></script><script defer src="bundle.js"></script><script defer src="https://static.cloudflareinsights.com/beacon.min.js/vcd15cbe7772f49c399c6a5babf22c1241717689176015" integrity="sha512-ZpsOmlRQV6y907TI0dKBHq9Md29nnaEIPlkf84rnaERnq6zvWvPUqr2ft8M1aS28oN72PdrCzSjY4U6VaAw1EQ==" data-cf-beacon='{"rayId":"980026701a6cb567","version":"2025.8.0","r":1,"token":"67f7a278e3374824ae6dd92295d38f77","serverTiming":{"name":{"cfExtPri":true,"cfEdge":true,"cfOrigin":true,"cfL4":true,"cfSpeedBrain":true,"cfCacheStatus":true}}}' crossorigin="anonymous"></script>
</body>
</html>

"""

---

## 3. MANDATO OPERATIVO (PARA LA IA)

Tu rol es el de un meticuloso **Frontend UI Specialist** con una gran atención al detalle. Tu misión es asegurar que nuestros componentes React sean visualmente idénticos a las plantillas de referencia de TailAdmin.

**PROTOCOLO DE REFACTORIZACIÓN (MANDATORIO):**
Debes seguir el siguiente proceso en tres fases. No pases a la siguiente fase sin la aprobación explícita del usuario.

### Fase 1: Análisis Diferencial
*   **Objetivo:** Identificar todas las discrepancias visuales y estructurales entre el componente React y el HTML de referencia.
*   **Acciones:**
    1.  Lee el contenido del archivo React en `{{RUTA_DEL_COMPONENTE_A_REFACTORIZAR}}`.
    2.  Analiza el `{{CODIGO_HTML_DE_REFERENCIA}}`.
    3.  Compara ambos y crea una **lista de diferencias**. Concéntrate en:
        *   **Clases de Tailwind CSS:** Diferencias en padding (`px-`, `py-`), márgenes (`mt-`), tamaños de fuente (`text-sm`, `text-title-md`), grosores de fuente (`font-medium`, `font-bold`), colores (`bg-`, `text-`), bordes, sombras, etc.
        *   **Estructura JSX/HTML:** Diferencias en el anidamiento de `divs` o el orden de los elementos que puedan afectar al layout.

*   **Salida:** Presenta esta lista de diferencias de forma clara y espera la confirmación para proceder.

### Fase 2: Plan de Refactorización
*   **Objetivo:** Traducir el análisis en un plan de acción concreto.
*   **Acciones:**
    1.  Basado en la lista de diferencias, formula un plan de refactorización punto por punto.
    2.  El plan debe ser explícito. Ejemplo:
        *   "En el `div` principal, reemplazaré la clase `p-4` por `py-6 px-7.5`."
        *   "En el `h4`, añadiré las clases `text-title-md font-bold`."
        *   "Reestructuraré el JSX para que el `div` del ícono esté al mismo nivel que el `div` del contenido, como en la referencia."

*   **Salida:** Presenta este plan de refactorización y espera la aprobación para proceder.

### Fase 3: Ejecución del Refactor
*   **Objetivo:** Aplicar los cambios planificados al archivo del componente.
*   **Acciones:**
    1.  Una vez aprobado el plan, modifica el archivo en `{{RUTA_DEL_COMPONENTE_A_REFACTORIZAR}}` para aplicar todos los cambios. **No modifiques la lógica (props, estado, etc.) a menos que sea estrictamente necesario para la nueva estructura JSX.**

*   **Salida:** Muestra el código completo y final del archivo refactorizado y confirma que la tarea ha sido completada.```

### Strategic Justification
*   **Propósito General**: Este prompt establece un proceso de "auditoría y corrección visual" riguroso y controlado. Transforma una tarea subjetiva ("haz que se vea mejor") en un proceso de ingeniería objetivo basado en la comparación con una fuente de verdad.
*   **Decisión de Diseño 1 (Patrón de Bloque de Configuración)**: El uso de `RUTA_DEL_COMPONENTE_A_REFACTORIZAR` y `CODIGO_HTML_DE_REFERENCIA` en un bloque editable simplifica enormemente el uso del prompt. El desarrollador solo necesita proporcionar los dos datos clave, reduciendo la fricción y la posibilidad de error.
*   **Decisión de Diseño 2 (Protocolo Trifásico como "Gated Execution")**: El flujo **Análisis -> Plan -> Ejecución** es una implementación directa y poderosa del principio de **"ejecución controlada"**.
    *   **Análisis:** Obliga a la IA a ser explícita sobre lo que está "mal" antes de intentar arreglarlo.
    *   **Plan:** Obliga a la IA a ser explícita sobre "cómo" lo va a arreglar. Esto le da al humano un punto de control crucial para validar el enfoque antes de que se modifique el código.
    *   **Ejecución:** Es la fase de acción, que ahora se basa en un plan previamente validado, minimizando el riesgo de un resultado incorrecto.
*   **Decisión de Diseño 3 (Enfoque en el "Delta")**: El prompt guía a la IA para que se concentre en las **diferencias (el delta)** entre el estado actual y el deseado. Este enfoque es mucho más eficiente que pedirle que reescriba el componente desde cero, ya que preserva la lógica de React existente (props, estado, etc.) y solo modifica la capa de presentación (JSX y `className`), que es exactamente el objetivo de la tarea.
*   **Consideración Adicional (Reutilización)**: Este prompt es una herramienta altamente reutilizable. Puede ser usado para auditar y alinear cualquier componente del proyecto con el sistema de diseño a lo largo de todo el ciclo de vida del desarrollo.
