const { SlashCommandBuilder } = require('@discordjs/builders');

module.exports = {
	data: new SlashCommandBuilder()
		.setName('ban')
		.setDescription('.')
		.addNumberOption(option =>
			option.setName('id')
				.setDescription('account id')
				.setRequired(true))
		.addStringOption(option =>
			option.setName('reason')
				.setDescription('Specify reason')
				.setRequired(true))
                .addNumberOption(option =>
                    option.setName('time')
                        .setDescription('In minutes (0 = permanent ban)')
                        .setRequired(true))

};