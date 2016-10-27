using System.Data.Entity;
using MySql.Data.Entity;
using MySql.Data.MySqlClient;

namespace MyChat
{
    public class User
    {
        public string Name { get; set; }
        public string MachineName { get; set; }
    }

    public class Message
    {
        public virtual User User { get; set; }
        public string Text { get; set; }
    }

    [DbConfigurationType(typeof(MySqlEFConfiguration))]
    public class ChatContext : DbContext
    {
        public ChatContext()
            : base(new MySqlConnection("server=BY1-WWV-220;port=3306;database=MyChat;uid=User;password=12345678"), false)
        {
            Database.CreateIfNotExists();
        }

        protected override void OnModelCreating(DbModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);
            modelBuilder.Entity<User>().MapToStoredProcedures();
            modelBuilder.Entity<Message>().MapToStoredProcedures();
        }

        public DbSet<User> Users { get; set; }
        public DbSet<Message> Messages { get; set; }
    }
}