package com.danimania.mineanarquia.itemsilegales.Mineanarquia;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Multimap;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.*;

import static org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH;

public final class ItemsIlegales extends JavaPlugin implements Listener {

    private ProtocolManager protocolManager;
    private Set<Material> materialesIlegales;
    private Set<Material> stacksIlegales;
    public void onLoad() {
        protocolManager = ProtocolLibrary.getProtocolManager();

    }

    @Override
    public void onEnable() {
        materialesIlegales = new HashSet<>();
        stacksIlegales = new HashSet<>();
        // creamos los materiales ilegales
        materialesIlegales.add(Material.BEDROCK);
        materialesIlegales.add(Material.BARRIER);
        materialesIlegales.add(Material.STRUCTURE_VOID);
        materialesIlegales.add(Material.SPAWNER);
        materialesIlegales.add(Material.LEGACY_MONSTER_EGG);
        materialesIlegales.add(Material.LEGACY_MONSTER_EGGS);
        materialesIlegales.add(Material.END_PORTAL_FRAME);
        materialesIlegales.add(Material.END_PORTAL);
        // vamos a quitar los spawner egg no?

        List<Material> materials = Arrays.asList(Material.values());
        for (Material m: materials) {
            // bloques ilegales. su existencia debe ser destruida
            if (m.name().contains("SPAWN_EGG") || m.name().contains("COMMAND")
                    || m.name().contains("STRUCTURE")) {
                materialesIlegales.add(m);
            }

            if (m.name().contains("SWORD") || m.name().contains("BOOTS") ||
                    m.name().contains("LEGGIN") || m.name().contains("CHESTPLATE") ||
                    m.name().contains("HELMET") || m.name().contains("AXE") ||
                    m.name().contains("HOE") || m.name().contains("SHIELD") ||
                    m.name().contains("TOTEM") ) {
                stacksIlegales.add(m);
            }

            // stacks ilegales. si hay mas de uno deben ser destruidos

        }

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        // antithunder
        this.protocolManager.addPacketListener((PacketListener)new PacketAdapter((Plugin)this, new PacketType[] { PacketType.Play.Server.NAMED_SOUND_EFFECT }) {
            public void onPacketSending(PacketEvent event) {

                // Sonidos que esten a mas de 250 del jugador seran cancelados? quien sabe
                try {
                    final Player player = event.getPlayer();
                    PacketContainer packet = event.getPacket();
                    StructureModifier<Integer> ints = packet.getIntegers();
                    int xPacket =  ints.read(0) / 8;
                    int zPacket = ints.read(2) / 8;
                    double xPlayer = player.getLocation().getBlockX();
                    double zPlayer = player.getLocation().getBlockZ();

                    double xDif = Math.abs(xPlayer - xPacket);
                    double yDif = Math.abs(zPlayer - zPacket);
                    if(xDif > 250 | yDif > 250) {
                        event.setCancelled(true);
                    }
                }
                catch(Exception e) {}


            }
        });


    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (label.equalsIgnoreCase("cama")) {
                Player p = (Player) sender;
                if (p.getBedSpawnLocation() != null) {
                    String mensaje = (p.getBedSpawnLocation().getWorld().getEnvironment().equals(World.Environment.NORMAL))
                            ? "Tu cama está en X: " + p.getBedSpawnLocation().getBlockX() + " Y: " + p.getBedSpawnLocation().getBlockY() + " Z: " + p.getBedSpawnLocation().getBlockZ()
                            : "Tu nexo de reaparición está en X: " + p.getBedSpawnLocation().getBlockX() + " Y: " + p.getBedSpawnLocation().getBlockY() + " Z: " + p.getBedSpawnLocation().getBlockZ();
                    p.sendMessage(ChatColor.DARK_GREEN + mensaje);
                } else {
                    p.sendMessage(ChatColor.RED + "No tienes cama o nexo de reaparición.");
                }
            }
            else if (label.equalsIgnoreCase("playtime")) {
                if (args.length < 2) {
                    Player p = (Player) sender;
                    if (args.length == 1) {
                        p = Bukkit.getPlayer(args[0]);
                    }
                    if (p != null) {
                        double horas = (double) p.getStatistic(Statistic.PLAY_ONE_MINUTE) / 72000;
                        sender.sendMessage(ChatColor.DARK_GREEN + p.getDisplayName() + " ha jugado " + Math.round(horas * 100.0) / 100.0 + " horas"); // redondeamos las horas a dos decimales
                    } else {
                        sender.sendMessage(ChatColor.RED + "Ese jugador no esta online!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Has escrito mal el comando.");
                }
            }
        }
        return true;
    }

    // cuando se suelta un objeto (romper cofre etc)
    @EventHandler
    public void dropear(BlockDropItemEvent e){
        for(Item i : e.getItems()){
            if(verificarIlegal(i.getItemStack())){
                i.getItemStack().setAmount(0);
            }
        }
    }

    @EventHandler
    public void jugadorInteractua(PlayerInteractEvent e){
        if(verificarIlegal(e.getItem())) {
            e.setCancelled(true);
        }
        revisarJugador(e.getPlayer());
    }

    @EventHandler
    public void moveObjectFromInventory (InventoryMoveItemEvent e) {
        if(verificarIlegal(e.getItem())) {
            e.getItem().setAmount(0);
        } else {
            if (
                    e.getSource().getType() == InventoryType.SHULKER_BOX && e.getDestination().getType() == InventoryType.HOPPER
            ) {
                e.setCancelled(true);
            }
        }


    }


    @EventHandler
    public void alEntrar(PlayerJoinEvent e){
        // verificarPocionesIlegales(e.getPlayer());
        revisarJugador(e.getPlayer());

    }

    @EventHandler
    public void alCambioMano(PlayerSwapHandItemsEvent e){
        revisarJugador(e.getPlayer());
    }

    @EventHandler
    public void alPortalUsar(PlayerPortalEvent e) {
        revisarJugador(e.getPlayer());
        if(e.getTo().getWorld().getEnvironment().equals(World.Environment.THE_END)){
            if(e.getFrom().getWorld().getEnvironment().equals(World.Environment.NETHER)){
                e.setCancelled(true);
                e.getPlayer().setHealth(1);
            }
            if(Math.abs(e.getFrom().getBlockX()) > 25000.0 || Math.abs(e.getFrom().getBlockZ()) > 25000.0){
                e.setCancelled(true);
                e.getPlayer().setHealth(1);
                // e.getPlayer().kickPlayer("tu portal no me gusta. En el futruo esto te matara");
            }
        }
    }


    @EventHandler
    public void romperEndPortal(BlockBreakEvent e){
        revisarJugador(e.getPlayer());
        if(e.getBlock().getType() == Material.END_PORTAL){
            e.setCancelled(true);
        }
    }
    
    
    @EventHandler
    public void alMoverse(PlayerMoveEvent e) {
        final Player p = e.getPlayer();
        revisarPosicionJugador(p);

    }


    @EventHandler
    public void alColocarBloque(BlockPlaceEvent e) {

        ItemStack items = new ItemStack(e.getBlock().getType());
        if(verificarIlegal(items)) {
            e.setCancelled(true);
            e.getPlayer().setHealth(1);
        }
        revisarJugador(e.getPlayer());
    }

    @EventHandler
    public void alAbrir(InventoryOpenEvent e){
        revisarJugador(e.getPlayer());
        for(ItemStack i: e.getPlayer().getInventory()){
            if (verificarIlegal(i)){
                i.setAmount(0);
            }
        }
        boolean ilegales = false;
        if(e.getInventory().getType() == InventoryType.HOPPER){
            for(ItemStack it : e.getPlayer().getInventory().getContents()){
                if(verificarIlegal(it)){
                    it.setAmount((0));
                    ilegales = true;
                }
            }
        }


        for(ItemStack i : e.getInventory().getContents()){
            if(verificarIlegal(i)){
                ilegales = true;
                i.setAmount(0);
            }


        }
        if(ilegales){
            Bukkit.getServer().getLogger().info("Items ilegales. Jugador: "+e.getPlayer().getName()+". X"+e.getPlayer().getLocation().getX()+", Y"+e.getPlayer().getLocation().getY()+", Z"+e.getPlayer().getLocation().getZ());
            e.getPlayer().setHealth(1);
        }

    }

    @EventHandler
    public void alCargarChunk(ChunkLoadEvent e){
        if(!e.isNewChunk()){
            for(Entity ent: e.getChunk().getEntities()) {
                verificarEntidadIlegal(ent);
            }
        }
    }

    public void verificarEntidadIlegal(Entity e ) {
        if(e.isInvulnerable()) {
            Bukkit.getServer().getLogger().warning("Posible entidad ilegal: "+ ". X"+e.getLocation().getX()+", Y"+e.getLocation().getY()+", Z"+e.getLocation().getZ());
            e.setInvulnerable(false);
            e.teleport(new Location(e.getLocation().getWorld(), 0, -20 , 0));
            e.remove();

        }

    }

    public boolean verificarIlegal(ItemStack item){
        // nombre ilegal ilegal!
        try {
            if(item.getItemMeta().hasDisplayName()){
                Multimap<Attribute, AttributeModifier> modificadores = item.getItemMeta().getAttributeModifiers();
                try {
                    for ( Attribute attr: modificadores.asMap().keySet()) {
                        for( AttributeModifier mod : modificadores.get(attr) ){
                            if(mod.getAmount() > 50.0) {
                                return true;
                            }
                        }
                    }
                } catch (Exception e) {

                }

                if(
                        item.getItemMeta().hasLore()
                ) {
                    for(String loreLine : item.getItemMeta().getLore()){
                        Bukkit.getServer().getLogger().warning("LORE LINE: " + loreLine);
                    }
                }

                String nombre = item.getItemMeta().getDisplayName().toLowerCase();
                if(nombre.contains("ilegal")
                        || nombre.contains("danimania")
                        || nombre.contains("fabrimania")
                        || nombre.contains("backdo")
                        || nombre.contains("32k")
                        || nombre.contains("unnamed")
                        || nombre.contains("wither")
                        || nombre.contains("stach")
                        || nombre.contains("z4st")
                        || nombre.contains("boom y")
                        || nombre.contains("god")
                        || nombre.contains("shacke")
                ){
                    return true;
                }

            }

        } catch (Exception e){
            
        }

        if(item != null){
            if(item.getType() == Material.FIREWORK_ROCKET){
                FireworkMeta fwm = (FireworkMeta) item.getItemMeta();
                if(fwm.getPower()>3){
                    return true;
                }
            }else if(
                    materialesIlegales.contains(item.getType())
            ){
                return true;
            }else if(item.getType() == Material.SPLASH_POTION){
                PotionMeta meta = (PotionMeta) item.getItemMeta();
                for(PotionEffect pe : meta.getCustomEffects()){
                    if(pe.getAmplifier()>4){
                        return true;
                    }
                }
            }

            if( stacksIlegales.contains(item.getType())){
                if(item.getAmount()>1){
                    return true;
                }
            }

            Map <Enchantment, Integer> enchants = item.getEnchantments();
            for (Enchantment e: enchants.keySet()) {
                if (e.getMaxLevel() < enchants.get(e)){
                    return true;
                }
            }
        }
        return false;
    }

    public Boolean ilegalStack(ItemStack item){
        Boolean res = false;

        return res;
    }

    public void checkPlayerSpeed( Player p) {

    }


    public void revisarJugador (HumanEntity p){
        if(p.getAttribute(GENERIC_MAX_HEALTH).getValue()> 36.0) {
            p.setHealth(1.0);
            p.getInventory().clear();
            Location location = p.getLocation();
            location.setY(-1000.);
            p.teleport(location);
        }
        for(ItemStack i : p.getInventory().getContents()){
            if(verificarIlegal(i)){
                p.setHealth(1.0);
                i.setAmount(0);

            }
        }

        //verificar EQUIPO
        for(ItemStack i : p.getInventory().getArmorContents()){
            if(verificarIlegal(i)){
                p.setHealth(1.0);
                i.setAmount(0);
            }
        }

        if(verificarIlegal(p.getInventory().getItemInOffHand())){
            p.setHealth(1.0);
            p.getInventory().getItemInOffHand().setAmount(0);
            // p.getInventory().clear();
        }
    }

    public void revisarPosicionJugador(Player p) {
        Location localizacion = p.getEyeLocation();
        int x = Math.abs(localizacion.getBlockX());
        int y = localizacion.getBlockY();
        int z = Math.abs(localizacion.getBlockZ());
        if (localizacion.getWorld().getName().toLowerCase().contains("nether") && (y < 0 || y > 128)) {
            if (x > 990000 || z > 990000) {
                // voy a simplemente molestar al usuario mandandole mensajes
                p.sendMessage("Advertencia: Sal del techo (o del vacío) antes de llegar a 1 millon.\n(O MORIRÁS!!!)");
            }
            if (x > 1000000 || z > 1000000) {
                // voy a simplemente molestar al usuario mandandole mensajes
                p.getInventory().clear();
                p.setHealth(0.0);
            }

        }
    }


}
