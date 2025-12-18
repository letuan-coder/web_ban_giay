using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Xml.Linq;

namespace buoi5.Models
{
    public partial class Hanghoa
    {
        public Hanghoa()
        {
            Chitietphieudathang = new HashSet<Chitietphieudathang>();
            Chitietphieugiaohang = new HashSet<Chitietphieugiaohang>();
        }
        [Required(ErrorMessage = "Xin nhập mã hàng hóa ")]// rang buoc nhap dl
        [Display(Name = "Mã hàng hóa")]
        public string Mahang { get; set; }
        [Required(ErrorMessage = "Xin nhập tên hàng hóa ")]// rang buoc nhap dl
        [Display(Name = "Tên hàng hóa")]
        public string Tenhang { get; set; }
        [Required(ErrorMessage = "Xin nhập đơn vị tính ")]// rang buoc nhap dl
        [Display(Name = "Đơn vị tính")]
        public string Donvitinh { get; set; }
        [Required(ErrorMessage = "Xin nhập đơn giá ")]// rang buoc nhap dl
        [Display(Name = "Đơn giá")]
        public double? Dongia { get; set; }
        
        [Display(Name = "Hình ảnh")]
        public string Hinh { get; set; }
        [Display(Name = "Mã loại")]
        public string Maloai { get; set; }
        [Display(Name = "Mã nhà sản xuất")]
        public string Mansx { get; set; }

        public Loaihanghoa MaloaiNavigation { get; set; }
        public Nhasanxuat MansxNavigation { get; set; }
        public ICollection<Chitietphieudathang> Chitietphieudathang { get; set; }
        public ICollection<Chitietphieugiaohang> Chitietphieugiaohang { get; set; }
    }
}
