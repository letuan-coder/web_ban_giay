using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace buoi5.Models
{
    public partial class Nhanvien
    {
        public Nhanvien()
        {
            Phieugiaohang = new HashSet<Phieugiaohang>();
        }

        [Required(ErrorMessage = "Xin nhập mã nhân viên ")]// rang buoc nhap dl
        [Display(Name = "Mã nhân viên")]
        public string Manv { get; set; }
        [Required(ErrorMessage = "Xin nhập tên nhân viên ")]// rang buoc nhap dl
        [Display(Name = "Tên nhân viên")]
        public string Tennv { get; set; }
        [Required(ErrorMessage = "Xin nhập ngày sinh ")]// rang buoc nhap dl
        [Display(Name = "Ngày sinh")]
        public DateTime? Ngaysinh { get; set; }
        public bool? Phai { get; set; }
        [Required(ErrorMessage = "Xin nhập địa chỉ ")]// rang buoc nhap dl
        [Display(Name = "Địa chỉ")]
        public string Diachi { get; set; }
        [Required(ErrorMessage = "Xin nhập password")]// rang buoc nhap dl
        [Display(Name = "Password")]
        public string Password { get; set; }

        public ICollection<Phieugiaohang> Phieugiaohang { get; set; }
    }
}
