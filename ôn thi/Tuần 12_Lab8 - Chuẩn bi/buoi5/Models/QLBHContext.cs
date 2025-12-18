using System;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata;

namespace buoi5.Models
{
    public partial class QLBHContext : DbContext
    {
        public QLBHContext()
        {
        }

        public QLBHContext(DbContextOptions<QLBHContext> options)
            : base(options)
        {
        }

        public virtual DbSet<Chitietphieudathang> Chitietphieudathang { get; set; }
        public virtual DbSet<Chitietphieugiaohang> Chitietphieugiaohang { get; set; }
        public virtual DbSet<Hanghoa> Hanghoa { get; set; }
        public virtual DbSet<Khachhang> Khachhang { get; set; }
        public virtual DbSet<Loaihanghoa> Loaihanghoa { get; set; }
        public virtual DbSet<Nhanvien> Nhanvien { get; set; }
        public virtual DbSet<Nhasanxuat> Nhasanxuat { get; set; }
        public virtual DbSet<Phieudathang> Phieudathang { get; set; }
        public virtual DbSet<Phieugiaohang> Phieugiaohang { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            if (!optionsBuilder.IsConfigured)
            {
#warning To protect potentially sensitive information in your connection string, you should move it out of source code. See http://go.microsoft.com/fwlink/?LinkId=723263 for guidance on storing connection strings.
                optionsBuilder.UseSqlServer("Data Source=DESKTOP-2HL8JVN\\SQLEXPRESS;Initial Catalog=QLBH;Integrated Security=True;");
            }
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Chitietphieudathang>(entity =>
            {
                entity.HasKey(e => new { e.Mapdh, e.Mahang });

                entity.ToTable("chitietphieudathang");

                entity.Property(e => e.Mapdh)
                    .HasColumnName("mapdh")
                    .HasMaxLength(50);

                entity.Property(e => e.Mahang)
                    .HasColumnName("mahang")
                    .HasMaxLength(50);

                entity.Property(e => e.Dongia).HasColumnName("dongia");

                entity.Property(e => e.Soluong).HasColumnName("soluong");

                entity.HasOne(d => d.MahangNavigation)
                    .WithMany(p => p.Chitietphieudathang)
                    .HasForeignKey(d => d.Mahang)
                    .OnDelete(DeleteBehavior.ClientSetNull)
                    .HasConstraintName("FK_chitietphieudathang_hanghoa");

                entity.HasOne(d => d.MapdhNavigation)
                    .WithMany(p => p.Chitietphieudathang)
                    .HasForeignKey(d => d.Mapdh)
                    .OnDelete(DeleteBehavior.ClientSetNull)
                    .HasConstraintName("FK_chitietphieudathang_phieudathang");
            });

            modelBuilder.Entity<Chitietphieugiaohang>(entity =>
            {
                entity.HasKey(e => new { e.Mapgh, e.Mahang });

                entity.ToTable("chitietphieugiaohang");

                entity.Property(e => e.Mapgh)
                    .HasColumnName("mapgh")
                    .HasMaxLength(50);

                entity.Property(e => e.Mahang)
                    .HasColumnName("mahang")
                    .HasMaxLength(50);

                entity.Property(e => e.Donvitinh)
                    .HasColumnName("donvitinh")
                    .HasMaxLength(50);

                entity.Property(e => e.Soluong).HasColumnName("soluong");

                entity.HasOne(d => d.MahangNavigation)
                    .WithMany(p => p.Chitietphieugiaohang)
                    .HasForeignKey(d => d.Mahang)
                    .OnDelete(DeleteBehavior.ClientSetNull)
                    .HasConstraintName("FK_chitietphieugiaohang_hanghoa");

                entity.HasOne(d => d.MapghNavigation)
                    .WithMany(p => p.Chitietphieugiaohang)
                    .HasForeignKey(d => d.Mapgh)
                    .OnDelete(DeleteBehavior.ClientSetNull)
                    .HasConstraintName("FK_chitietphieugiaohang_phieugiaohang");
            });

            modelBuilder.Entity<Hanghoa>(entity =>
            {
                entity.HasKey(e => e.Mahang);

                entity.ToTable("hanghoa");

                entity.Property(e => e.Mahang)
                    .HasColumnName("mahang")
                    .HasMaxLength(50)
                    .ValueGeneratedNever();

                entity.Property(e => e.Dongia).HasColumnName("dongia");

                entity.Property(e => e.Donvitinh)
                    .HasColumnName("donvitinh")
                    .HasMaxLength(50);

                entity.Property(e => e.Hinh)
                    .HasColumnName("hinh")
                    .HasMaxLength(50);

                entity.Property(e => e.Maloai)
                    .HasColumnName("maloai")
                    .HasMaxLength(50);

                entity.Property(e => e.Mansx)
                    .HasColumnName("mansx")
                    .HasMaxLength(50);

                entity.Property(e => e.Tenhang)
                    .HasColumnName("tenhang")
                    .HasMaxLength(50);

                entity.HasOne(d => d.MaloaiNavigation)
                    .WithMany(p => p.Hanghoa)
                    .HasForeignKey(d => d.Maloai)
                    .HasConstraintName("FK_hanghoa_loaihanghoa");

                entity.HasOne(d => d.MansxNavigation)
                    .WithMany(p => p.Hanghoa)
                    .HasForeignKey(d => d.Mansx)
                    .HasConstraintName("FK_hanghoa_nhasanxuat");
            });

            modelBuilder.Entity<Khachhang>(entity =>
            {
                entity.HasKey(e => e.Makh);

                entity.ToTable("khachhang");

                entity.Property(e => e.Makh)
                    .HasColumnName("makh")
                    .HasMaxLength(50)
                    .ValueGeneratedNever();

                entity.Property(e => e.Diachi)
                    .HasColumnName("diachi")
                    .HasMaxLength(50);

                entity.Property(e => e.Dienthoai)
                    .HasColumnName("dienthoai")
                    .HasMaxLength(50);

                entity.Property(e => e.Namsinh).HasColumnName("namsinh");

                entity.Property(e => e.Password)
                    .HasColumnName("password")
                    .HasMaxLength(50);

                entity.Property(e => e.Phai).HasColumnName("phai");

                entity.Property(e => e.Tenkh)
                    .HasColumnName("tenkh")
                    .HasMaxLength(50);
            });

            modelBuilder.Entity<Loaihanghoa>(entity =>
            {
                entity.HasKey(e => e.Maloai);

                entity.ToTable("loaihanghoa");

                entity.Property(e => e.Maloai)
                    .HasColumnName("maloai")
                    .HasMaxLength(50)
                    .ValueGeneratedNever();

                entity.Property(e => e.Tenloai)
                    .HasColumnName("tenloai")
                    .HasMaxLength(50);
            });

            modelBuilder.Entity<Nhanvien>(entity =>
            {
                entity.HasKey(e => e.Manv);

                entity.ToTable("nhanvien");

                entity.Property(e => e.Manv)
                    .HasColumnName("manv")
                    .HasMaxLength(50)
                    .ValueGeneratedNever();

                entity.Property(e => e.Diachi)
                    .HasColumnName("diachi")
                    .HasMaxLength(50);

                entity.Property(e => e.Ngaysinh)
                    .HasColumnName("ngaysinh")
                    .HasColumnType("datetime");

                entity.Property(e => e.Password)
                    .HasColumnName("password")
                    .HasMaxLength(50);

                entity.Property(e => e.Phai).HasColumnName("phai");

                entity.Property(e => e.Tennv)
                    .HasColumnName("tennv")
                    .HasMaxLength(50);
            });

            modelBuilder.Entity<Nhasanxuat>(entity =>
            {
                entity.HasKey(e => e.Mansx);

                entity.ToTable("nhasanxuat");

                entity.Property(e => e.Mansx)
                    .HasColumnName("mansx")
                    .HasMaxLength(50)
                    .ValueGeneratedNever();

                entity.Property(e => e.Diachi)
                    .HasColumnName("diachi")
                    .HasMaxLength(50);

                entity.Property(e => e.Tennsx)
                    .HasColumnName("tennsx")
                    .HasMaxLength(50);
            });

            modelBuilder.Entity<Phieudathang>(entity =>
            {
                entity.HasKey(e => e.Mapdh);

                entity.ToTable("phieudathang");

                entity.Property(e => e.Mapdh)
                    .HasColumnName("mapdh")
                    .HasMaxLength(50)
                    .ValueGeneratedNever();

                entity.Property(e => e.Diachigh)
                    .HasColumnName("diachigh")
                    .HasMaxLength(50);

                entity.Property(e => e.Makh)
                    .HasColumnName("makh")
                    .HasMaxLength(50);

                entity.Property(e => e.Ngaydh)
                    .HasColumnName("ngaydh")
                    .HasColumnType("datetime");

                entity.Property(e => e.Ngaygh)
                    .HasColumnName("ngaygh")
                    .HasColumnType("datetime");

                entity.HasOne(d => d.MakhNavigation)
                    .WithMany(p => p.Phieudathang)
                    .HasForeignKey(d => d.Makh)
                    .HasConstraintName("FK_phieudathang_khachhang");
            });

            modelBuilder.Entity<Phieugiaohang>(entity =>
            {
                entity.HasKey(e => e.Mapgh);

                entity.ToTable("phieugiaohang");

                entity.Property(e => e.Mapgh)
                    .HasColumnName("mapgh")
                    .HasMaxLength(50)
                    .ValueGeneratedNever();

                entity.Property(e => e.Diachigh)
                    .HasColumnName("diachigh")
                    .HasMaxLength(50);

                entity.Property(e => e.Manv)
                    .HasColumnName("manv")
                    .HasMaxLength(50);

                entity.Property(e => e.Mapdh)
                    .HasColumnName("mapdh")
                    .HasMaxLength(50);

                entity.Property(e => e.Ngaygh)
                    .HasColumnName("ngaygh")
                    .HasColumnType("datetime");

                entity.Property(e => e.Tennguoinhan)
                    .HasColumnName("tennguoinhan")
                    .HasMaxLength(50);

                entity.HasOne(d => d.ManvNavigation)
                    .WithMany(p => p.Phieugiaohang)
                    .HasForeignKey(d => d.Manv)
                    .HasConstraintName("FK_phieugiaohang_nhanvien");

                entity.HasOne(d => d.MapdhNavigation)
                    .WithMany(p => p.Phieugiaohang)
                    .HasForeignKey(d => d.Mapdh)
                    .HasConstraintName("FK_phieugiaohang_phieudathang");
            });
        }
    }
}
