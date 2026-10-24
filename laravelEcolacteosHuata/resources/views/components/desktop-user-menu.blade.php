@props(['showTeam' => true])

<flux:dropdown position="bottom" align="end">
    <button type="button" class="group flex items-center gap-2 rounded-lg p-1 hover:bg-zinc-800/5 dark:hover:bg-white/10" data-test="user-menu-button">
        <flux:avatar :name="auth()->user()->nombreCompleto()" size="sm" />
        <div class="hidden text-start text-sm leading-tight sm:grid">
            <span class="truncate font-medium text-zinc-600 group-hover:text-zinc-900 dark:text-white/80 dark:group-hover:text-white">{{ auth()->user()->nombreCompleto() }}</span>
            <span class="truncate text-xs text-zinc-400 dark:text-zinc-500">{{ auth()->user()->rol?->label() }}</span>
        </div>
        <flux:icon name="chevron-down" variant="micro" class="hidden size-4 text-zinc-400 sm:block" />
    </button>

    <flux:menu>
        <div class="flex items-center gap-2 px-1 py-1.5 text-start text-sm">
            <flux:avatar
                :name="auth()->user()->nombreCompleto()"
            />
            <div class="grid flex-1 text-start text-sm leading-tight">
                <flux:heading class="truncate">{{ auth()->user()->nombreCompleto() }}</flux:heading>
                <flux:text class="truncate">{{ auth()->user()->rol?->label() }}</flux:text>
            </div>
        </div>
        <flux:menu.separator />
        <flux:menu.radio.group>
            <flux:menu.item :href="route('profile.edit')" icon="cog" wire:navigate>
                {{ __('Settings') }}
            </flux:menu.item>
            <form method="POST" action="{{ route('logout') }}" class="w-full">
                @csrf
                <flux:menu.item
                    as="button"
                    type="submit"
                    icon="arrow-right-start-on-rectangle"
                    class="w-full cursor-pointer"
                    data-test="logout-button"
                >
                    {{ __('Log out') }}
                </flux:menu.item>
            </form>
        </flux:menu.radio.group>
    </flux:menu>
</flux:dropdown>
