import os
import math
from PIL import Image, ImageDraw, ImageFilter

def create_master_icon(size=1024, is_round=False):
    # Create high-res canvas
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # 1. Background Gradient / Surface
    bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg_draw = ImageDraw.Draw(bg)
    
    # Render diagonal vibrant royal blue to deep indigo-violet gradient
    # Top-left: #2563EB (Royal Blue), Center: #1D4ED8, Bottom-Right: #0F172A (Deep Slate Violet)
    for y in range(size):
        for x in range(size):
            # Diagonal factor
            t = (x * 0.75 + y * 1.25) / (size * 2.0)
            t = max(0.0, min(1.0, t))
            
            # Colors
            # Color 0 (0.0): #3B82F6 (59, 130, 246)
            # Color 1 (0.5): #1D4ED8 (29, 78, 216)
            # Color 2 (1.0): #0F172A (15, 23, 42)
            if t < 0.5:
                sub_t = t / 0.5
                r = int(59 + (29 - 59) * sub_t)
                g = int(130 + (78 - 130) * sub_t)
                b = int(246 + (216 - 246) * sub_t)
            else:
                sub_t = (t - 0.5) / 0.5
                r = int(29 + (15 - 29) * sub_t)
                g = int(78 + (23 - 78) * sub_t)
                b = int(216 + (42 - 216) * sub_t)
            bg.putpixel((x, y), (r, g, b, 255))
            
    # Add ambient inner radial glow in top-left
    glow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    center_glow = (int(size * 0.35), int(size * 0.25))
    glow_radius = int(size * 0.6)
    for r in range(glow_radius, 0, -2):
        alpha = int(45 * (1.0 - r / glow_radius))
        glow_draw.ellipse([center_glow[0] - r, center_glow[1] - r, center_glow[0] + r, center_glow[1] + r], fill=(255, 255, 255, alpha))
    
    bg = Image.alpha_composite(bg, glow)
    
    # Mask to rounded squircle (Apple-style continuous curve) or Circle
    mask = Image.new("L", (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)
    if is_round:
        mask_draw.ellipse([int(size * 0.04), int(size * 0.04), int(size * 0.96), int(size * 0.96)], fill=255)
    else:
        # Squircle / smooth corner radius
        corner_radius = int(size * 0.22)
        pad = int(size * 0.02)
        mask_draw.rounded_rectangle([pad, pad, size - pad, size - pad], radius=corner_radius, fill=255)
        
    bg.putalpha(mask)
    
    # 2. Draw 3D Elevated Foreground Element:
    # A glowing heart with protective hands and a central communication touch handset
    fg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    fg_draw = ImageDraw.Draw(fg)
    
    # Center Coordinates
    cx, cy = size // 2, int(size * 0.51)
    scale = size / 512.0
    
    # Shadow layer for the heart/touch glyph
    shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    
    def draw_care_emblem(target_draw, offset_x=0, offset_y=0, fill_color=(255, 255, 255, 255), stroke_width=int(18 * scale)):
        # Draw protective heart / caring arch
        # Top-left lobe, top-right lobe, meeting at bottom
        heart_pts = []
        for angle_deg in range(0, 360, 2):
            rad = math.radians(angle_deg)
            # Mathematical heart parametric equation
            # x = 16 * sin^3(t)
            # y = 13 * cos(t) - 5 * cos(2t) - 2 * cos(3t) - cos(4t)
            hx = 16 * (math.sin(rad) ** 3)
            hy = -(13 * math.cos(rad) - 5 * math.cos(2*rad) - 2 * math.cos(3*rad) - math.cos(4*rad))
            
            px = cx + offset_x + int(hx * 9.5 * scale)
            py = cy - int(20 * scale) + offset_y + int(hy * 9.5 * scale)
            heart_pts.append((px, py))
            
        target_draw.polygon(heart_pts, fill=fill_color)
        
    # Draw soft drop shadow behind heart
    draw_care_emblem(shadow_draw, offset_x=0, offset_y=int(14 * scale), fill_color=(0, 0, 0, 110))
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=int(16 * scale)))
    
    # Combine shadow with background
    composite = Image.alpha_composite(bg, shadow)
    
    # Draw glossy inner heart badge (Gradient: Pure White to Radiant Warm Pearl/Cyan #E0F2FE)
    heart_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    h_draw = ImageDraw.Draw(heart_layer)
    draw_care_emblem(h_draw, offset_x=0, offset_y=0, fill_color=(255, 255, 255, 255))
    
    # Inside the heart, draw the Caring Hand & Touch Phone Icon in Rich Azure Blue (#1D4ED8 / #2563EB)
    phone_layer = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    p_draw = ImageDraw.Draw(phone_layer)
    
    # Phone / Connection icon inside heart:
    # Classic angled smooth phone handset with a heart pulse / signal dot
    handset_color = (29, 78, 216, 255) # Deep Royal Blue
    
    # Modern Phone handset arc
    phone_cx, phone_cy = cx, cy - int(18 * scale)
    
    # Draw stylized phone handset
    # Left ear piece circle
    ear_r = int(24 * scale)
    p_draw.ellipse([phone_cx - int(52 * scale) - ear_r, phone_cy - int(38 * scale) - ear_r, phone_cx - int(52 * scale) + ear_r, phone_cy - int(38 * scale) + ear_r], fill=handset_color)
    # Right mic piece circle
    p_draw.ellipse([phone_cx + int(52 * scale) - ear_r, phone_cy + int(24 * scale) - ear_r, phone_cx + int(52 * scale) + ear_r, phone_cy + int(24 * scale) + ear_r], fill=handset_color)
    
    # Connecting bridge / curved handle
    p_draw.arc([phone_cx - int(70 * scale), phone_cy - int(55 * scale), phone_cx + int(70 * scale), phone_cy + int(45 * scale)], start=25, end=155, fill=handset_color, width=int(26 * scale))
    
    # Central warm caring pulse dot (Vibrant Amber/Gold #F59E0B)
    gold_color = (245, 158, 11, 255)
    pulse_r = int(14 * scale)
    p_draw.ellipse([phone_cx - pulse_r, phone_cy - int(6 * scale) - pulse_r, phone_cx + pulse_r, phone_cy - int(6 * scale) + pulse_r], fill=gold_color)
    
    # Heart layer composite
    heart_composite = Image.alpha_composite(heart_layer, phone_layer)
    
    # Add glossy top sheen
    sheen = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(sheen)
    sheen_pts = [
        (cx - int(120 * scale), cy - int(110 * scale)),
        (cx + int(120 * scale), cy - int(110 * scale)),
        (cx + int(60 * scale), cy - int(30 * scale)),
        (cx - int(60 * scale), cy - int(30 * scale))
    ]
    s_draw.polygon(sheen_pts, fill=(255, 255, 255, 45))
    sheen = sheen.filter(ImageFilter.GaussianBlur(radius=int(10 * scale)))
    
    final_fg = Image.alpha_composite(heart_composite, sheen)
    final_icon = Image.alpha_composite(composite, final_fg)
    
    return final_icon

def export_all():
    base_res = r"c:\Users\heysa\Documents\Dev\AMM\app\src\main\res"
    
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    
    print("Generating master icons...")
    master_square = create_master_icon(size=1024, is_round=False)
    master_round = create_master_icon(size=1024, is_round=True)
    
    # Save 512x512 Play Store icon
    playstore_icon = master_square.resize((512, 512), Image.Resampling.LANCZOS)
    playstore_icon.save(os.path.join(base_res, "ic_launcher-playstore.png"), "PNG")
    print("Saved 512x512 Play Store icon.")
    
    for folder, dim in densities.items():
        folder_path = os.path.join(base_res, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        sq = master_square.resize((dim, dim), Image.Resampling.LANCZOS)
        sq.save(os.path.join(folder_path, "ic_launcher.png"), "PNG")
        
        rd = master_round.resize((dim, dim), Image.Resampling.LANCZOS)
        rd.save(os.path.join(folder_path, "ic_launcher_round.png"), "PNG")
        print(f"Generated {folder} ({dim}x{dim})")

if __name__ == "__main__":
    export_all()
