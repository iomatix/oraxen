package io.th0rgal.oraxen.mechanics.provided.energyblast;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.settings.Message;
import io.th0rgal.oraxen.utils.VectorUtils;
import io.th0rgal.oraxen.utils.timers.Timer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;


public class EnergyBlastMechanicManager implements Listener {

    private final MechanicFactory factory;

    public EnergyBlastMechanicManager(MechanicFactory factory) {
        this.factory = factory;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {

        if(!(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)))
            return;

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item == null)
            return;

        String itemID = OraxenItems.getIdByItem(item);

        if (factory.isNotImplementedIn(itemID))
            return;

        EnergyBlastMechanic mechanic = (EnergyBlastMechanic) factory.getMechanic(itemID);

        Player player = event.getPlayer();

        Timer playerTimer = mechanic.getTimer(player);

        if (!playerTimer.isFinished()) {
            Message.DELAY.send(player, Timer.DECIMAL_FORMAT.format(playerTimer.getRemainingTimeMillis() / 1000D));
            return;
        }

        playerTimer.reset();

        Location origin = player.getEyeLocation();
        Vector direction = origin.getDirection();
        direction.normalize();
        direction.multiply(0.1);
        Location destination = origin.clone().add(direction);
        for (int i = 0; i < mechanic.getLength() * 10; i++) {
            Location loc = destination.add(direction);
            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, mechanic.getParticleColor());
        }

        playEffect(player, mechanic.getParticleColor(), mechanic.getDamage(), mechanic.getLength());
    }

    private Location getRightHandLocation(Player player){
        double yawRightHandDirection = Math.toRadians(-1 * player.getEyeLocation().getYaw() - 45);
        double x = 0.5 * Math.sin(yawRightHandDirection) + player.getLocation().getX();
        double y = player.getLocation().getY() + 1;
        double z = 0.5 * Math.cos(yawRightHandDirection) + player.getLocation().getZ();
        return new Location(player.getWorld(), x, y, z);
    }

    private void playEffect(Player player, Particle.DustOptions particleColor, double damage, int length) {
        new BukkitRunnable() {
            int circlePoints = 360;
            double radius = 2;
            Location playerLoc = player.getEyeLocation();
            World world = playerLoc.getWorld();
            final Vector dir = player.getLocation().getDirection().normalize();
            final double pitch = (playerLoc.getPitch() + 90.0F) * 0.017453292F;
            final double yaw = -playerLoc.getYaw() * 0.017453292F;
            double increment = (2 * Math.PI) / circlePoints;
            double circlePointOffset = 0;
            int beamLength = length * 2;
            double radiusShrinkage = radius / (double) ((beamLength + 2) / 2);
            @Override
            public void run() {
                beamLength--;
                if(beamLength < 1){
                    this.cancel();
                    return;
                }
                for (int i = 0; i < circlePoints; i++) {
                    double angle = i * increment + circlePointOffset;
                    double x = radius * Math.cos(angle);
                    double z = radius * Math.sin(angle);
                    Vector vec = new Vector(x, 0, z);
                    VectorUtils.rotateAroundAxisX(vec, pitch);
                    VectorUtils.rotateAroundAxisY(vec, yaw);
                    playerLoc.add(vec);
                    world.spawnParticle(Particle.REDSTONE, playerLoc, 0, 0, 0, 0, 0, particleColor);
                    playerLoc.subtract(vec);
                }

                circlePointOffset += increment / 3;
                if (circlePointOffset >= increment) {
                    circlePointOffset = 0;
                }

                radius -= radiusShrinkage;
                if (radius < 0) {
                    playerLoc.getWorld().spawnParticle(Particle.REDSTONE, playerLoc, 1000,0.3,0.3,0.3,0.3, particleColor);
                    for(Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, 0.5, 0.5, 0.5))
                        if(entity instanceof LivingEntity && entity != player)
                            ((LivingEntity)entity).damage(damage * 3.0);
                    this.cancel();
                    return;
                }
                playerLoc.add(dir);
                for(Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, radius, radius, radius))
                    if(entity instanceof LivingEntity && entity != player)
                        ((LivingEntity)entity).damage(damage);

            }
        }.runTaskTimer(OraxenPlugin.get(), 0, 1);
    }


}
