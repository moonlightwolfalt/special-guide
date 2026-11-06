SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Safe to run more than once: it only creates the table if it is missing.
-- The account server runs it on startup.
--

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(225) NOT NULL,
  `password_token` longtext NOT NULL,
  `discordCode` int(11) DEFAULT NULL,
  `seasondata` varchar(225) DEFAULT NULL,
  `discordTag` varchar(226) DEFAULT NULL,
  `discordID` varchar(225) DEFAULT NULL,
  `coins` int(11) DEFAULT 0,
  `xp` int(11) DEFAULT 0,
  `level` int(11) NOT NULL DEFAULT 1,
  `admin` int(11) DEFAULT 0,
  `limited` int(11) NOT NULL DEFAULT 0,
  `warns` varchar(225) DEFAULT NULL,
  `warn_executors` varchar(225) DEFAULT NULL,
  `banned` int(11) DEFAULT 0,
  `banreason` varchar(225) DEFAULT NULL,
  `banexecutor` varchar(225) DEFAULT NULL,
  `banTime` varchar(225) DEFAULT NULL,
  `discordAdmin` varchar(225) DEFAULT NULL,
  `expirydate` varchar(225) DEFAULT NULL,
  `name` varchar(225) NOT NULL,
  `gold_blackDragon` int(11) DEFAULT NULL,
  `gold_kingDragon` int(11) DEFAULT NULL,
  `gold_Dragon` int(11) DEFAULT NULL,
  `gold_kraken` int(11) DEFAULT NULL,
  `gold_yeti` int(11) DEFAULT NULL,
  `gold_trex` int(11) DEFAULT NULL,
  `gold_kingCrab` int(11) DEFAULT NULL,
  `gold_phoenix` int(11) DEFAULT NULL,
  `gold_pterodactyl` int(11) DEFAULT NULL,
  `gold_iceMonster` int(11) DEFAULT NULL,
  `gold_dinoMonster` int(11) DEFAULT NULL,
  `gold_seaMonster` int(11) DEFAULT NULL,
  `gold_landMonster` int(11) DEFAULT NULL,
  `gold_giantScorion` int(11) DEFAULT NULL,
  `gold_bluewhale` int(11) DEFAULT NULL,
  `gold_elephant` int(11) DEFAULT NULL,
  `gold_cassowary` int(11) DEFAULT NULL,
  `gold_giantspider` int(11) DEFAULT NULL,
  `gold_blackwidow` int(11) DEFAULT NULL,
  `gold_mammoth` int(11) DEFAULT NULL,
  `gold_hippo` int(11) DEFAULT NULL,
  `gold_ostrich` int(11) DEFAULT NULL,
  `gold_boa` int(11) DEFAULT NULL,
  `gold_komodo` int(11) DEFAULT NULL,
  `gold_killerwhale` int(11) DEFAULT NULL,
  `gold_sabertooth` int(11) DEFAULT NULL,
  `skin_orangeScorpion` int(11) DEFAULT NULL,
  `skin_blueDragon` int(11) DEFAULT NULL,
  `skin_kingRipper` int(11) DEFAULT NULL,
  `skin_kingStan` int(11) DEFAULT NULL,
  `skin_kingShah` int(11) DEFAULT NULL,
  `skin_dragon_gray` int(11) DEFAULT NULL,
  `skin_kraken_cyan` int(11) DEFAULT NULL,
  `skin_yeti_emerald` int(11) DEFAULT NULL,
  `skin_snowman` int(11) DEFAULT NULL,
  `skin_trex_gray` int(11) DEFAULT NULL,
  `skin_kingCrab` int(11) DEFAULT NULL,
  `skin_phoenix_blue` int(11) DEFAULT NULL,
  `skin_pterodactyl_green` int(11) DEFAULT NULL,
  `skin_lavaStrider` int(11) DEFAULT NULL,
  `skin_boneKing` int(11) DEFAULT NULL,
  `skin_dragon_terminator` int(11) DEFAULT NULL,
  `skin_dragon_red` int(11) DEFAULT NULL,
  `skin_iceMonster` int(11) DEFAULT NULL,
  `skin_dinoMonster` int(11) DEFAULT NULL,
  `skin_seaMonster` int(11) DEFAULT NULL,
  `skin_landMonster` int(11) DEFAULT NULL,
  `skin_bluewhale` int(11) DEFAULT NULL,
  `skin_elephant` int(11) DEFAULT NULL,
  `skin_cassowary` int(11) DEFAULT NULL,
  `skin_giantspider` int(11) DEFAULT NULL,
  `skin_blackwidow` int(11) DEFAULT NULL,
  `skin_mammoth` int(11) DEFAULT NULL,
  `skin_hippo` int(11) DEFAULT NULL,
  `skin_ostrich` int(11) DEFAULT NULL,
  `skin_boa` int(11) DEFAULT NULL,
  `skin_komodo` int(11) DEFAULT NULL,
  `skin_killerwhale` int(11) DEFAULT NULL,
  `skin_sabertooth` int(11) DEFAULT NULL,
  `skin_tigerShark` int(11) DEFAULT NULL,
  `skin_hamerHeadShark` int(11) DEFAULT NULL,
  `skin_panda` int(11) DEFAULT NULL,
  `skin_zonkey` int(11) DEFAULT NULL,
  `skin_okapi` int(11) DEFAULT NULL,
  `skin_giantCroc` int(11) DEFAULT NULL,
  `skin_purpleMouse` int(11) DEFAULT NULL,
  `skin_starMouse` int(11) DEFAULT NULL,
  `skin_winterMouse` int(11) DEFAULT NULL,
  `skin_prismaToucan` int(11) DEFAULT NULL,
  `gold_mouse` int(11) DEFAULT NULL,
  `skin_queenScarlet` int(11) DEFAULT NULL,
  `skin_queenRipper` int(11) DEFAULT NULL,
  `skin_sapphireMouse` int(11) DEFAULT NULL,
  `skin_amethystMouse` int(11) DEFAULT NULL,
  `skin_emeraldMouse` int(11) DEFAULT NULL,
  `skin_diamondMouse` int(11) DEFAULT NULL,
  `skin_partycrock` int(11) DEFAULT NULL,
  `skin_retrocrocodilealligator` int(11) DEFAULT NULL,
  `skin_rubblealligator` int(11) DEFAULT NULL,
  `skin_rubyblackdragon` int(11) DEFAULT NULL,
  `skin_kingGolder` int(11) DEFAULT NULL,
  `skin_kingInferno` int(11) DEFAULT NULL,
  `skin_queenCeleste` int(11) DEFAULT NULL,
  `skin_bigFoot` int(11) DEFAULT NULL,
  `skin_woollyRhino` int(11) DEFAULT NULL,
  `skin_banditMouse` int(11) DEFAULT NULL,
  `skin_chocolatebirthdaycakeMouse` int(11) DEFAULT NULL,
  `skin_strawberrybirthdaycakeMouse` int(11) DEFAULT NULL,
  `skin_chocolateDragon` int(11) DEFAULT NULL,
  `skin_neonDragon` int(11) DEFAULT NULL,
  `skin_galaxyPhoenix` int(11) DEFAULT NULL,
  `skin_meltedMonster` int(11) DEFAULT NULL,
  `skin_giftboxKraken` int(11) DEFAULT NULL,
  `social` varchar(225) NOT NULL,
  `email_isVerified` varchar(225) NOT NULL,
  `profilePicUrl` varchar(225) NOT NULL,
  `locale` varchar(225) DEFAULT NULL,
  `persistentName` varchar(225) DEFAULT NULL,
  `adminType` int(11) NOT NULL DEFAULT 0,
  `youtube` int(11) NOT NULL DEFAULT 0,
  `artist` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
